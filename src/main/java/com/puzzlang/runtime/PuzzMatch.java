package com.puzzlang.runtime;

import java.util.*;
import java.util.regex.*;

/**
 * PuzzMatch — Runtime pattern matching engine for PuzzLang.
 *
 * Handles string pattern matching with capture placeholders:
 *   Pattern: "move {n} from {a} to {b}"
 *   Input:   "move 5 from 2 to 3"
 *   Result:  {n: 5, a: 2, b: 3}
 *
 * Captures are automatically typed:
 *   - Numeric strings become Integer
 *   - Everything else stays as String
 */
public class PuzzMatch {

    // Pattern to find {name} placeholders in templates
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([a-zA-Z_][a-zA-Z0-9_]*)}");

    /**
     * Result of a pattern match attempt.
     */
    public static class MatchResult {
        private final boolean matched;
        private final Map<String, Object> captures;

        public static final MatchResult NO_MATCH = new MatchResult(false, Collections.emptyMap());

        private MatchResult(boolean matched, Map<String, Object> captures) {
            this.matched = matched;
            this.captures = captures;
        }

        public static MatchResult success(Map<String, Object> captures) {
            return new MatchResult(true, captures);
        }

        public boolean matched() {
            return matched;
        }

        public Map<String, Object> captures() {
            return captures;
        }

        public Object get(String name) {
            return captures.get(name);
        }

        @Override
        public String toString() {
            return matched ? "Match" + captures : "NoMatch";
        }
    }

    /**
     * Try to match an input string against a pattern template.
     *
     * @param input The string to match against
     * @param template The pattern template with {name} placeholders
     * @param captureNames List of capture names in order they appear in the template
     * @return MatchResult with captured values if matched, NO_MATCH otherwise
     *
     * Example:
     *   tryMatch("move 5 from 2 to 3", "move {n} from {a} to {b}", ["n", "a", "b"])
     *   → MatchResult{matched=true, captures={n=5, a=2, b=3}}
     */
    public static MatchResult tryMatch(String input, String template, List<String> captureNames) {
        if (input == null || template == null) {
            return MatchResult.NO_MATCH;
        }

        // Convert template to regex
        // "move {n} from {a}" → "move (.+?) from (.+?)"
        StringBuilder regex = new StringBuilder();
        int lastEnd = 0;
        Matcher m = PLACEHOLDER.matcher(template);

        while (m.find()) {
            // Quote literal text before this placeholder
            regex.append(Pattern.quote(template.substring(lastEnd, m.start())));
            // Add capture group - use (.+?) for non-greedy matching
            // But if this is the last capture and there's no text after, use (.+)
            regex.append("(.+?)");
            lastEnd = m.end();
        }

        // Quote any remaining literal text
        if (lastEnd < template.length()) {
            regex.append(Pattern.quote(template.substring(lastEnd)));
        }

        // Anchor the pattern
        Pattern compiled;
        try {
            compiled = Pattern.compile("^" + regex + "$");
        } catch (PatternSyntaxException e) {
            // Invalid pattern, can't match
            return MatchResult.NO_MATCH;
        }

        // Try to match
        Matcher inputMatcher = compiled.matcher(input);
        if (!inputMatcher.matches()) {
            return MatchResult.NO_MATCH;
        }

        // Verify we have the right number of groups
        if (inputMatcher.groupCount() != captureNames.size()) {
            return MatchResult.NO_MATCH;
        }

        // Extract captures into map
        Map<String, Object> captures = new LinkedHashMap<>();
        for (int i = 0; i < captureNames.size(); i++) {
            String value = inputMatcher.group(i + 1);
            captures.put(captureNames.get(i), parseValue(value));
        }

        return MatchResult.success(captures);
    }

    /**
     * Try to parse a captured value as a number.
     * Returns Integer if possible, otherwise returns the original String.
     */
    private static Object parseValue(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }

        // Try integer first
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            // Not an integer
        }

        // Try long for larger numbers
        try {
            long l = Long.parseLong(s);
            // If it fits in int, return int, otherwise long
            if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                return (int) l;
            }
            return l;
        } catch (NumberFormatException e) {
            // Not a long either
        }

        // Return as string
        return s;
    }

    /**
     * Utility method to create a list of capture names.
     * Used by bytecode emitter.
     */
    public static List<String> captureList(String... names) {
        return Arrays.asList(names);
    }
}
