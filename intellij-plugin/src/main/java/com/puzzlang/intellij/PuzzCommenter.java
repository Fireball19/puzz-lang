package com.puzzlang.intellij;

import com.intellij.lang.Commenter;
import org.jetbrains.annotations.Nullable;

/**
 * Commenter implementation for PuzzLang.
 * Enables Ctrl+/ (Cmd+/ on Mac) to comment/uncomment lines.
 */
public class PuzzCommenter implements Commenter {

    @Nullable
    @Override
    public String getLineCommentPrefix() {
        return "# ";
    }

    @Nullable
    @Override
    public String getBlockCommentPrefix() {
        return null; // PuzzLang doesn't have block comments
    }

    @Nullable
    @Override
    public String getBlockCommentSuffix() {
        return null;
    }

    @Nullable
    @Override
    public String getCommentedBlockCommentPrefix() {
        return null;
    }

    @Nullable
    @Override
    public String getCommentedBlockCommentSuffix() {
        return null;
    }
}
