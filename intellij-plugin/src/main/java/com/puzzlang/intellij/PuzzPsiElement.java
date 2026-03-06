package com.puzzlang.intellij;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

/**
 * Base PSI element for PuzzLang.
 */
public class PuzzPsiElement extends ASTWrapperPsiElement {

    public PuzzPsiElement(@NotNull ASTNode node) {
        super(node);
    }
}
