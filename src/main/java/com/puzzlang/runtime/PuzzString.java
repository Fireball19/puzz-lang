package com.puzzlang.runtime;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * PuzzString — static utility methods for String operations in PuzzLang.
 *
 * These are called via method dispatch on String receivers:
 *   "hello\nworld".lines()      → PuzzList ["hello", "world"]
 *   "a\n\nb".paragraphs()       → PuzzList ["a", "b"]
 *   "a,b,c".split(",")          → PuzzList ["a", "b", "c"]
 */
public class PuzzString {

    // Pattern for splitting paragraphs (one or more blank lines)
    private static final Pattern PARAGRAPH_SPLIT = Pattern.compile("\\n\\s*\\n");

    /**
     * Split a string into lines.
     * Handles both Unix (\n) and Windows (\r\n) line endings.
     *
     * @param s the input string
     * @return PuzzList of lines (empty strings preserved for blank lines)
     */
    public static PuzzList lines(String s) {
        if (s == null || s.isEmpty()) {
            return new PuzzList();
        }
        // Normalize line endings and split
        String normalized = s.replace("\r\n", "\n").replace("\r", "\n");
        // Remove trailing newline to avoid empty last element
        if (normalized.endsWith("\n")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        String[] parts = normalized.split("\n", -1);  // -1 keeps trailing empties
        return new PuzzList(Arrays.asList(parts));
    }

    /**
     * Split a string into paragraphs (separated by blank lines).
     * Very common in AoC puzzles where input has groups separated by empty lines.
     *
     * @param s the input string
     * @return PuzzList of paragraphs (trimmed)
     */
    public static PuzzList paragraphs(String s) {
        if (s == null || s.isEmpty()) {
            return new PuzzList();
        }
        // Normalize line endings
        String normalized = s.replace("\r\n", "\n").replace("\r", "\n").trim();
        String[] parts = PARAGRAPH_SPLIT.split(normalized);
        PuzzList result = new PuzzList();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    /**
     * Split a string by a delimiter.
     *
     * @param s the input string
     * @param delimiter the delimiter to split on
     * @return PuzzList of parts
     */
    public static PuzzList split(String s, String delimiter) {
        if (s == null || s.isEmpty()) {
            return new PuzzList();
        }
        // Use Pattern.quote to handle special regex characters in delimiter
        String[] parts = s.split(Pattern.quote(delimiter), -1);
        return new PuzzList(Arrays.asList(parts));
    }

    /**
     * Split a string by whitespace (one or more spaces/tabs).
     * Common for AoC inputs with space-separated values.
     *
     * @param s the input string
     * @return PuzzList of non-empty parts
     */
    public static PuzzList words(String s) {
        if (s == null || s.isEmpty()) {
            return new PuzzList();
        }
        String[] parts = s.trim().split("\\s+");
        PuzzList result = new PuzzList();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.add(part);
            }
        }
        return result;
    }

    /**
     * Split a string into individual characters.
     *
     * @param s the input string
     * @return PuzzList of single-character strings
     */
    public static PuzzList chars(String s) {
        if (s == null || s.isEmpty()) {
            return new PuzzList();
        }
        PuzzList result = new PuzzList();
        for (char c : s.toCharArray()) {
            result.add(String.valueOf(c));
        }
        return result;
    }

    /**
     * Parse string as integer.
     *
     * @param s the input string
     * @return parsed integer value (boxed)
     */
    public static Integer toInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("PuzzLang: cannot parse '" + s + "' as integer");
        }
    }

    /**
     * Check if string starts with prefix.
     */
    public static boolean startsWith(String s, String prefix) {
        return s != null && s.startsWith(prefix);
    }

    /**
     * Check if string ends with suffix.
     */
    public static boolean endsWith(String s, String suffix) {
        return s != null && s.endsWith(suffix);
    }

    /**
     * Check if string contains substring.
     */
    public static boolean contains(String s, String substring) {
        return s != null && s.contains(substring);
    }

    /**
     * Replace all occurrences of a substring.
     */
    public static String replace(String s, String target, String replacement) {
        if (s == null) return null;
        return s.replace(target, replacement);
    }
}