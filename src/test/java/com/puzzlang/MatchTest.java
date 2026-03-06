package com.puzzlang;

import com.puzzlang.api.CompiledUnit;
import com.puzzlang.api.PuzzCompiler;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Match statement pattern matching in PuzzLang:
 *   - Basic pattern matching with captures
 *   - Multiple arms with different patterns
 *   - Wildcard/default arms
 *   - Automatic type conversion of captures
 *   - Match in loops
 *   - Edge cases
 */
class MatchTest {

    private String run(String source) throws Exception {
        PuzzCompiler compiler = new PuzzCompiler();
        CompiledUnit unit = compiler.compile(source, "T");

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream old = System.out;
        System.setOut(new PrintStream(buf));
        try {
            unit.execute();
        } finally {
            System.setOut(old);
        }
        return buf.toString().stripTrailing();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Basic Pattern Matching
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void basicPatternMatch() throws Exception {
        String src = """
            match "move 5 from 2 to 3":
                "move {n} from {a} to {b}" -> print n
            """;
        assertEquals("5", run(src));
    }

    @Test
    void captureMultipleValues() throws Exception {
        String src = """
            match "move 5 from 2 to 3":
                "move {n} from {a} to {b}" -> print a
            """;
        assertEquals("2", run(src));
    }

    @Test
    void captureLastValue() throws Exception {
        String src = """
            match "move 5 from 2 to 3":
                "move {n} from {a} to {b}" -> print b
            """;
        assertEquals("3", run(src));
    }

    @Test
    void useCaptureInExpression() throws Exception {
        String src = """
            match "add 10 to 20":
                "add {a} to {b}" -> print a + b
            """;
        assertEquals("30", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Multiple Arms
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void multipleArmsFirstMatch() throws Exception {
        String src = """
            match "move 5":
                "move {n}" -> print "moving " + n
                "turn {dir}" -> print "turning " + dir
            """;
        assertEquals("moving 5", run(src));
    }

    @Test
    void multipleArmsSecondMatch() throws Exception {
        String src = """
            match "turn left":
                "move {n}" -> print "moving " + n
                "turn {dir}" -> print "turning " + dir
            """;
        assertEquals("turning left", run(src));
    }

    @Test
    void multipleArmsNoMatch() throws Exception {
        String src = """
            match "jump high":
                "move {n}" -> print "moving"
                "turn {dir}" -> print "turning"
            """;
        // No output expected when no arm matches
        assertEquals("", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Wildcard/Default Arms
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void wildcardCatchAll() throws Exception {
        String src = """
            match "something else":
                "move {n}" -> print "move"
                _ -> print "default"
            """;
        assertEquals("default", run(src));
    }

    @Test
    void wildcardNotUsedWhenPatternMatches() throws Exception {
        String src = """
            match "move 5":
                "move {n}" -> print "move " + n
                _ -> print "default"
            """;
        assertEquals("move 5", run(src));
    }

    @Test
    void wildcardOnly() throws Exception {
        String src = """
            match "anything":
                _ -> print "caught"
            """;
        assertEquals("caught", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Type Conversion of Captures
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void capturesAsIntegers() throws Exception {
        String src = """
            match "value 42":
                "value {n}" -> print n + 8
            """;
        assertEquals("50", run(src));
    }

    @Test
    void capturesAsStrings() throws Exception {
        String src = """
            match "name Alice":
                "name {n}" -> print "Hello, " + n
            """;
        assertEquals("Hello, Alice", run(src));
    }

    @Test
    void mixedCaptures() throws Exception {
        String src = """
            match "user Alice has 100 points":
                "user {name} has {points} points" -> print name + ": " + points
            """;
        assertEquals("Alice: 100", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Match with Variables
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void matchVariableSubject() throws Exception {
        String src = """
            let cmd = "move 10"
            match cmd:
                "move {n}" -> print n
            """;
        assertEquals("10", run(src));
    }

    @Test
    void matchExpressionSubject() throws Exception {
        String src = """
            let prefix = "turn "
            let suffix = "left"
            match prefix + suffix:
                "turn {dir}" -> print dir
            """;
        assertEquals("left", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Match in Loops
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void matchInForLoop() throws Exception {
        String src = """
            for line in "move 1\\nmove 2\\nmove 3".lines():
                match line:
                    "move {n}" -> print n
            """;
        assertEquals("1\n2\n3", run(src));
    }

    @Test
    void matchWithAccumulator() throws Exception {
        String src = """
            let sum = 0
            for line in "add 10\\nadd 20\\nadd 30".lines():
                match line:
                    "add {n}" -> let sum = sum + n
            print sum
            """;
        assertEquals("60", run(src));
    }

    @Test
    void matchMixedCommands() throws Exception {
        String src = """
            let total = 0
            for line in "add 5\\nsub 2\\nadd 10".lines():
                match line:
                    "add {n}" -> let total = total + n
                    "sub {n}" -> let total = total - n
            print total
            """;
        assertEquals("13", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  AoC-Style Patterns
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void aocMovePattern() throws Exception {
        // Typical AoC Day 5 crane puzzle pattern
        String src = """
            match "move 3 from 1 to 2":
                "move {count} from {src} to {dst}" -> print count + " items from stack " + src
            """;
        assertEquals("3 items from stack 1", run(src));
    }

    @Test
    void aocRangePattern() throws Exception {
        // Typical AoC range pattern like "2-4,6-8"
        String src = """
            match "2-4":
                "{start}-{end}" -> print start + " to " + end
            """;
        assertEquals("2 to 4", run(src));
    }

    @Test
    void aocCoordinatePattern() throws Exception {
        String src = """
            match "10,20":
                "{x},{y}" -> print "x=" + x + " y=" + y
            """;
        assertEquals("x=10 y=20", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Edge Cases
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void emptyCapture() throws Exception {
        // When the captured value is empty
        String src = """
            match "value ":
                "value {n}" -> print "got: [" + n + "]"
            """;
        // This should not match because .+? requires at least one character
        assertEquals("", run(src));
    }

    @Test
    void singleCharCapture() throws Exception {
        String src = """
            match "x5":
                "x{n}" -> print n
            """;
        assertEquals("5", run(src));
    }

    @Test
    void patternWithSpecialChars() throws Exception {
        String src = """
            match "price: $100":
                "price: ${amount}" -> print amount
            """;
        assertEquals("100", run(src));
    }

    @Test
    void noCaptures() throws Exception {
        String src = """
            match "hello world":
                "hello world" -> print "exact match"
                _ -> print "no match"
            """;
        assertEquals("exact match", run(src));
    }

    @Test
    void firstArmWins() throws Exception {
        // When multiple patterns could match, first one wins
        String src = """
            match "value 5":
                "value {n}" -> print "first: " + n
                "value 5" -> print "second"
            """;
        assertEquals("first: 5", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Complex Patterns
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void threeCaptures() throws Exception {
        String src = """
            match "from A to B via C":
                "from {start} to {end} via {waypoint}" -> print start + "->" + waypoint + "->" + end
            """;
        assertEquals("A->C->B", run(src));
    }

    @Test
    void repeatedDelimiter() throws Exception {
        String src = """
            match "a:b:c":
                "{x}:{y}:{z}" -> print y
            """;
        assertEquals("b", run(src));
    }

    @Test
    void matchWithPrint() throws Exception {
        // Ensure captured variables are accessible in print
        String src = """
            match "test 42":
                "test {val}" -> print "Value is " + val
            """;
        assertEquals("Value is 42", run(src));
    }
}
