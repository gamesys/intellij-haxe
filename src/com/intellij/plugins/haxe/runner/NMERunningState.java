package com.intellij.plugins.haxe.runner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.plugins.haxe.HaxeBundle;
import com.intellij.plugins.haxe.config.sdk.HaxeSdkData;
import com.intellij.plugins.haxe.ide.module.HaxeModuleSettings;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author: Fedor.Korotkov
 */
public class NMERunningState extends CommandLineState {
  private final Module module;

  public NMERunningState(ExecutionEnvironment env, Module module) {
    super(env);
    this.module = module;
  }

  @NotNull
  @Override
  protected ProcessHandler startProcess() throws ExecutionException {
    final HaxeModuleSettings settings = HaxeModuleSettings.getInstance(module);
    final Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
    assert sdk != null;

    GeneralCommandLine commandLine = getCommandForNeko(sdk, settings);

    return new OSProcessHandler(commandLine.createProcess(), commandLine.getCommandLineString());
  }

  private GeneralCommandLine getCommandForNeko(Sdk sdk, HaxeModuleSettings settings) throws ExecutionException {
    final HaxeSdkData sdkData = sdk.getSdkAdditionalData() instanceof HaxeSdkData ? (HaxeSdkData)sdk.getSdkAdditionalData() : null;
    final GeneralCommandLine commandLine = new GeneralCommandLine();

    commandLine.setWorkDirectory(PathUtil.getParentPath(module.getModuleFilePath()));
    assert sdkData != null;
    final String haxelibPath = sdkData.getHaxelibPath();
    if (haxelibPath == null || haxelibPath.isEmpty()) {
      throw new ExecutionException(HaxeBundle.message("no.haxelib.for.sdk", sdk.getName()));
    }
    commandLine.setExePath(haxelibPath);
    commandLine.addParameter("run");
    commandLine.addParameter("nme");
    commandLine.addParameter("run");
    commandLine.addParameter(settings.getNmmlPath());
    for (String flag : settings.getNmeTarget().getFlags()) {
      commandLine.addParameter(flag);
    }

    final TextConsoleBuilder consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(module.getProject());
    setConsoleBuilder(consoleBuilder);
    return commandLine;
  }
}