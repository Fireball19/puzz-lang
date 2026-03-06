package com.puzzlang.intellij;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * Icon definitions for PuzzLang plugin.
 */
public class PuzzIcons {
    
    /**
     * 16x16 icon for .puzz files in project tree and tabs.
     */
    public static final Icon FILE = IconLoader.getIcon("/icons/puzz-file.svg", PuzzIcons.class);
    
    /**
     * 13x13 icon for tool windows (optional).
     */
    public static final Icon TOOL_WINDOW = IconLoader.getIcon("/icons/puzz-file.svg", PuzzIcons.class);
}
