package com.puzzlang.intellij;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Custom element type for PuzzLang AST nodes.
 */
public class PuzzElementType extends IElementType {

    public PuzzElementType(@NotNull @NonNls String debugName) {
        super(debugName, PuzzLanguage.INSTANCE);
    }
}
