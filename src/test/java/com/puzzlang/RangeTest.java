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