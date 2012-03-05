package com.intellij.plugins.haxe.ide.formatter;

import com.intellij.formatting.Indent;
import com.intellij.lang.ASTNode;
import com.intellij.plugins.haxe.util.UsefulPsiTreeUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Nullable;

import static com.intellij.plugins.haxe.lang.lexer.HaxeTokenTypeSets.COMMENTS;
import static com.intellij.plugins.haxe.lang.lexer.HaxeTokenTypeSets.FUNCTION_DEFINITION;
import static com.intellij.plugins.haxe.lang.lexer.HaxeTokenTypes.*;

/**
 * @author: Fedor.Korotkov
 */
public class HaxeIndentProcessor {
  private final CommonCodeStyleSettings settings;

  public HaxeIndentProcessor(CommonCodeStyleSettings settings) {
    this.settings = settings;
  }

  public Indent getChildIndent(ASTNode node) {
    final IElementType elementType = node.getElementType();
    final ASTNode prevSibling = UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(node);
    final IElementType prevSiblingType = prevSibling == null ? null : prevSibling.getElementType();
    final ASTNode parent = node.getTreeParent();
    final IElementType parentType = parent != null ? parent.getElementType() : null;
    final ASTNode superParent = parent == null ? null : parent.getTreeParent();
    final IElementType superParentType = superParent == null ? null : superParent.getElementType();

    final int braceStyle = FUNCTION_DEFINITION.contains(superParentType) ? settings.METHOD_BRACE_STYLE : settings.BRACE_STYLE;

    if (parent == null || parent.getTreeParent() == null) {
      return Indent.getNoneIndent();
    }
    if (COMMENTS.contains(elementType)) {
      if (settings.KEEP_FIRST_COLUMN_COMMENT) {
        return Indent.getAbsoluteNoneIndent();
      }
      return Indent.getNormalIndent();
    }
    if (elementType == PLCURLY || elementType == PRCURLY) {
      switch (braceStyle) {
        case CommonCodeStyleSettings.END_OF_LINE:
        case CommonCodeStyleSettings.NEXT_LINE:
        case CommonCodeStyleSettings.NEXT_LINE_IF_WRAPPED:
          return Indent.getNoneIndent();
        case CommonCodeStyleSettings.NEXT_LINE_SHIFTED:
        case CommonCodeStyleSettings.NEXT_LINE_SHIFTED2:
          return Indent.getNormalIndent();
        default:
          return Indent.getNoneIndent();
      }
    }
    if (parentType == HAXE_PARENTHESIZEDEXPRESSION) {
      if (elementType == PLPAREN || elementType == PRPAREN) {
        return Indent.getNoneIndent();
      }
      return Indent.getNormalIndent();
    }
    if (needIndent(parentType)) {
      final PsiElement psi = node.getPsi();
      if (psi.getParent() instanceof PsiFile) {
        return Indent.getNoneIndent();
      }
      return Indent.getNormalIndent();
    }
    if (FUNCTION_DEFINITION.contains(parentType) || parentType == HAXE_CALLEXPRESSION) {
      if (elementType == HAXE_PARAMETERLIST || elementType == HAXE_EXPRESSIONLIST) {
        return Indent.getNormalIndent();
      }
    }
    if (parentType == HAXE_FORSTATEMENT && prevSiblingType == PRPAREN && elementType != HAXE_BLOCKSTATEMENT) {
      return Indent.getNormalIndent();
    }
    if (parentType == HAXE_WHILESTATEMENT && prevSiblingType == PRPAREN && elementType != HAXE_BLOCKSTATEMENT) {
      return Indent.getNormalIndent();
    }
    if (parentType == HAXE_DOWHILESTATEMENT && prevSiblingType == KDO && elementType != HAXE_BLOCKSTATEMENT) {
      return Indent.getNormalIndent();
    }
    if ((parentType == HAXE_RETURNSTATEMENT || parentType == HAXE_RETURNSTATEMENTWITHOUTSEMICOLON) &&
        prevSiblingType == KRETURN &&
        elementType != HAXE_BLOCKSTATEMENT) {
      return Indent.getNormalIndent();
    }
    if (parentType == HAXE_IFSTATEMENT && (prevSiblingType == PRPAREN || prevSiblingType == KELSE) && elementType != HAXE_BLOCKSTATEMENT) {
      return Indent.getNormalIndent();
    }
    return Indent.getNoneIndent();
  }

  private static boolean needIndent(@Nullable IElementType type) {
    if (type == null) {
      return false;
    }
    boolean result = type == HAXE_BLOCKSTATEMENT;
    result = result || type == HAXE_CLASSBODY;
    result = result || type == HAXE_ENUMBODY;
    result = result || type == HAXE_INTERFACEBODY;
    return result;
  }
}