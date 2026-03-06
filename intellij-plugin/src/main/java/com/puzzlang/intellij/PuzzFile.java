package com.puzzlang.intellij;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

/**
 * PSI file representation for PuzzLang source files.
 */
public class PuzzFile extends PsiFileBase {

    public PuzzFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, PuzzLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return PuzzFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "PuzzLang File";
    }
}
