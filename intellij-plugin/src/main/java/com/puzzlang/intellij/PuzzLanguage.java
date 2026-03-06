package com.puzzlang.intellij;

import com.intellij.lang.Language;

/**
 * Language definition for PuzzLang.
 */
public class PuzzLanguage extends Language {

    public static final PuzzLanguage INSTANCE = new PuzzLanguage();

    private PuzzLanguage() {
        super("PuzzLang");
    }
}
