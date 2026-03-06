package com.puzzlang.intellij;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Brace matcher for PuzzLang.
 * Provides matching bracket highlighting and navigation.
 */
public class PuzzBraceMatcher implements PairedBraceMatcher {

    private static final BracePair[] PAIRS = new BracePair[]{
            new BracePair(PuzzTokenTypes.LPAREN, PuzzTokenTypes.RPAREN, false),
            new BracePair(PuzzTokenTypes.LBRACKET, PuzzTokenTypes.RBRACKET, false),
            new BracePair(PuzzTokenTypes.LBRACE, PuzzTokenTypes.RBRACE, true)
    };

    @Override
    public BracePair @NotNull [] getPairs() {
        return PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
        return true;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }
}
