package com.puzzlang.intellij;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Custom token type for PuzzLang tokens.
 */
public class PuzzTokenType extends IElementType {

    public PuzzTokenType(@NotNull @NonNls String debugName) {
        super(debugName, PuzzLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "PuzzTokenType." + super.toString();
    }
}
