package com.puzzlang;

import com.puzzlang.compiler.Compiler;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for first-class range support.
 * Covers: literals, methods, for-in, overlaps, sum formula.
 */
class RangeTest {

    private String run(String source) throws Exception {
        Compiler c = new Compiler();
        byte[] bc  = c.compile(source, "T");
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream old = System.out;
        System.setOut(new PrintStream(buf));
        try   { c.execute(bc, "T"); }
        finally { System.setOut(old); }
        return buf.toString().stripTrailing();
    }

    // ── Literals ─────────────────────────────────────────────────────────────

    @Test void exclusiveRangePrint() throws Exception {
        assertEquals("1..5",   run("let r = 1..5\nprint r\n"));
    }

    @Test void inclusiveRangePrint() throws Exception {
        assertEquals("1..=5",  run("let r = 1..=5\nprint r\n"));
    }

    // ── count ────────────────────────────────────────────────────────────────

    @Test void exclusiveCount() throws Exception {
        // 1..5 → {1,2,3,4} → 4
        assertEquals("4",  run("print (1..5).count()\n"));
    }

    @Test void inclusiveCount() throws Exception {
        // 1..=5 → {1,2,3,4,5} → 5
        assertEquals("5",  run("print (1..=5).count()\n"));
    }

    // ── sum ──────────────────────────────────────────────────────────────────

    @Test void exclusiveSum() throws Exception {
        // 1+2+3+4 = 10
        assertEquals("10", run("print (1..5).sum()\n"));
    }

    @Test void inclusiveSum() throws Exception {
        // 1+2+...+100 = 5050
        assertEquals("5050", run("print (1..=100).sum()\n"));
    }

    @Test void gaussSum() throws Exception {
        // Gauss: 1..101 (exclusive) = 1+2+...+100 = 5050
        assertEquals("5050", run("print (1..101).sum()\n"));
    }

    // ── min / max ────────────────────────────────────────────────────────────

    @Test void minMax() throws Exception {
        assertEquals("1",  run("print (1..=10).min()\n"));
        assertEquals("10", run("print (1..=10).max()\n"));
    }

    // ── contains ─────────────────────────────────────────────────────────────

    @Test void exclusiveContains() throws Exception {
        assertEquals("true",  run("print (1..10).contains(5)\n"));
        assertEquals("false", run("print (1..10).contains(10)\n")); // exclusive!
        assertEquals("false", run("print (1..10).contains(0)\n"));
    }

    @Test void inclusiveContains() throws Exception {
        assertEquals("true",  run("print (1..=10).contains(10)\n")); // inclusive
        assertEquals("false", run("print (1..=10).contains(11)\n"));
    }

    // ── overlaps ─────────────────────────────────────────────────────────────

    @Test void overlapsTrue() throws Exception {
        // {1..9} and {5..14} share elements
        assertEquals("true",
                run("let a = 1..10\nlet b = 5..15\nprint a.overlaps(b)\n"));
    }

    @Test void overlapsFalse() throws Exception {
        // {1..4} and {5..9} don't share elements
        assertEquals("false",
                run("let a = 1..5\nlet b = 5..10\nprint a.overlaps(b)\n"));
    }

    @Test void overlapsAdjacent() throws Exception {
        // {1..=5} and {5..=10} share element 5
        assertEquals("true",
                run("let a = 1..=5\nlet b = 5..=10\nprint a.overlaps(b)\n"));
    }

    // ── for-in ───────────────────────────────────────────────────────────────

    @Test void forInExclusive() throws Exception {
        assertEquals("0\n1\n2\n3\n4",
                run("for x in 0..5:\n    print x\n"));
    }

    @Test void forInInclusive() throws Exception {
        assertEquals("1\n2\n3",
                run("for x in 1..=3:\n    print x\n"));
    }

    @Test void forInSum() throws Exception {
        // Sum 0+1+2+3+4 = 10
        String src =
                "let s = 0\n" +
                        "for x in 0..5:\n" +
                        "    let s = s + x\n" +
                        "print s\n";
        assertEquals("10", run(src));
    }

    // ── Additional for-in tests ──────────────────────────────────────────────

    @Test void forInWithExistingVar() throws Exception {
        // Variable declared before loop, updated inside
        String src =
                "let total = 0\n" +
                        "for i in 1..=5:\n" +
                        "    let total = total + i\n" +
                        "print total\n";
        assertEquals("15", run(src));
    }

    @Test void forInNested() throws Exception {
        // Nested for-in loops
        String src =
                "let sum = 0\n" +
                        "for i in 1..=2:\n" +
                        "    for j in 1..=3:\n" +
                        "        let sum = sum + i * j\n" +
                        "print sum\n";
        // i=1: j=1,2,3 -> 1+2+3=6
        // i=2: j=1,2,3 -> 2+4+6=12
        // total = 18
        assertEquals("18", run(src));
    }

    @Test void forInMultipleVars() throws Exception {
        // Multiple variables, one updated in loop
        String src =
                "let a = 10\n" +
                        "let b = 0\n" +
                        "for x in 1..4:\n" +
                        "    let b = b + x\n" +
                        "print a\n" +
                        "print b\n";
        assertEquals("10\n6", run(src));
    }

    @Test void forInLoopVarShadow() throws Exception {
        // Loop variable shadows outer variable
        String src =
                "let x = 100\n" +
                        "let sum = 0\n" +
                        "for x in 1..=3:\n" +
                        "    let sum = sum + x\n" +
                        "print sum\n" +
                        "print x\n";
        // sum = 1+2+3 = 6
        // x after loop is 3 (last iteration value) since we reuse the slot
        assertEquals("6\n3", run(src));
    }

    @Test void forInSimplePrint() throws Exception {
        // Just print, no variable updates
        assertEquals("1\n2\n3", run("for n in 1..=3:\n    print n\n"));
    }

    @Test void forInWithMethodCall() throws Exception {
        // Use method call result in loop
        String src =
                "let r = 1..=3\n" +
                        "let s = 0\n" +
                        "for v in r:\n" +
                        "    let s = s + v\n" +
                        "print s\n" +
                        "print r.sum()\n";
        assertEquals("6\n6", run(src));
    }

    @Test void consecutiveForLoops() throws Exception {
        // Two separate for loops
        String src =
                "let a = 0\n" +
                        "for i in 1..=3:\n" +
                        "    let a = a + i\n" +
                        "let b = 0\n" +
                        "for j in 1..=4:\n" +
                        "    let b = b + j\n" +
                        "print a\n" +
                        "print b\n";
        assertEquals("6\n10", run(src));
    }

    // ── variables holding ranges ──────────────────────────────────────────────

    @Test void rangeInVariable() throws Exception {
        String src =
                "let r = 1..=5\n" +
                        "print r.sum()\n" +
                        "print r.count()\n";
        assertEquals("15\n5", run(src));
    }

    // ── isEmpty ───────────────────────────────────────────────────────────────

    @Test void emptyRange() throws Exception {
        assertEquals("true",  run("print (5..5).isEmpty()\n"));   // 5..5 has no elements
        assertEquals("false", run("print (5..=5).isEmpty()\n"));  // 5..=5 has one element: 5
    }
}