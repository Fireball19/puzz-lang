package com.puzzlang.intellij;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * File type definition for PuzzLang (.puzz) files.
 */
public class PuzzFileType extends LanguageFileType {

    public static final PuzzFileType INSTANCE = new PuzzFileType();

    private PuzzFileType() {
        super(PuzzLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "PuzzLang";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "PuzzLang source file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "puzz";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return PuzzIcons.FILE;
    }
}
