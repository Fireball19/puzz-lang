package com.puzzlang.frontend;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Converts Python-style indentation into INDENT/DEDENT tokens
 * injected as literal text so ANTLR can parse them.
 */
public class IndentPreprocessor {

    public static String process(String source) {
        source = source.replace("\r\n", "\n").replace("\r", "\n");
        String[] lines   = source.split("\n", -1);
        StringBuilder out = new StringBuilder();
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(0);

        for (String line : lines) {
            String trimmed = line.stripTrailing();
            if (trimmed.isEmpty() || trimmed.stripLeading().startsWith("#")) continue;

            int indent  = leadingSpaces(line);
            int current = stack.peek();

            if (indent > current) {
                stack.push(indent);
                out.append("INDENT\n");
            } else {
                while (indent < stack.peek()) {
                    stack.pop();
                    out.append("DEDENT\n");
                }
            }
            out.append(trimmed.stripLeading()).append("\n");
        }

        while (stack.peek() > 0) {
            stack.pop();
            out.append("DEDENT\n");
        }
        return out.toString();
    }

    private static int leadingSpaces(String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if      (c == ' ')  count++;
            else if (c == '\t') count += 4;
            else break;
        }
        return count;
    }
}
