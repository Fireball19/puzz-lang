package com.puzzlang.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * PuzzIO — I/O functions for PuzzLang.
 *
 * Provides:
 *   stdin()          → read all of standard input as a string
 *   readFile(path)   → read file contents as a string
 *
 * These return String values which can then use .lines(), .paragraphs(), etc.
 */
public class PuzzIO {

    /**
     * Read all content from standard input.
     * Blocks until EOF (Ctrl+D on Unix, Ctrl+Z on Windows).
     *
     * @return entire stdin as a single String
     */
    public static Object stdin() {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (!first) {
                    sb.append("\n");
                }
                sb.append(line);
                first = false;
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException("PuzzLang: error reading stdin: " + e.getMessage());
        }
    }

    /**
     * Read entire contents of a file.
     *
     * @param pathObj the file path (String)
     * @return file contents as a single String
     */
    public static Object readFile(Object pathObj) {
        String pathStr = String.valueOf(pathObj);
        try {
            Path path = Paths.get(pathStr);
            if (!Files.exists(path)) {
                throw new RuntimeException("PuzzLang: file not found: " + pathStr);
            }
            if (!Files.isReadable(path)) {
                throw new RuntimeException("PuzzLang: cannot read file: " + pathStr);
            }
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException("PuzzLang: error reading file '" + pathStr + "': " + e.getMessage());
        }
    }

    /**
     * Write content to a file.
     * 
     * @param pathObj the file path (String)
     * @param content the content to write
     * @return the path that was written to
     */
    public static Object writeFile(Object pathObj, Object content) {
        String pathStr = String.valueOf(pathObj);
        String contentStr = String.valueOf(content);
        try {
            Path path = Paths.get(pathStr);
            Files.writeString(path, contentStr);
            return pathStr;
        } catch (IOException e) {
            throw new RuntimeException("PuzzLang: error writing file '" + pathStr + "': " + e.getMessage());
        }
    }
}
