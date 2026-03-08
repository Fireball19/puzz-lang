package com.puzzlang;

import com.puzzlang.api.CompiledUnit;
import com.puzzlang.api.PuzzCompiler;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Tuple functionality in PuzzLang:
 *   - Tuple literals: (1, 2), (a, b, c)
 *   - Destructuring in let: let (x, y) = point
 *   - Destructuring in for: for (x, y) in points
 *   - Tuple pattern matching: match coord: (0, 0) -> ...
 *   - Multiple assignment: let a, b = b, a
 *   - Tuple methods: .get(), .first(), .second(), .size()
 */
class TupleTest {

    private String run(String source) throws Exception {
        PuzzCompiler compiler = new PuzzCompiler();
        CompiledUnit unit = compiler.compile(source, "TupleTest");
        
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
    //  Tuple Literals
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void tuplePairLiteral() throws Exception {
        assertEquals("(1, 2)", run("print (1, 2)\n"));
    }

    @Test
    void tupleTripleLiteral() throws Exception {
        assertEquals("(1, 2, 3)", run("print (1, 2, 3)\n"));
    }

    @Test
    void tupleWithVariables() throws Exception {
        String src = """
            let x = 10
            let y = 20
            print (x, y)
            """;
        assertEquals("(10, 20)", run(src));
    }

    @Test
    void tupleWithExpressions() throws Exception {
        String src = """
            let point = (1 + 2, 3 * 4)
            print point
            """;
        assertEquals("(3, 12)", run(src));
    }

    @Test
    void nestedTuples() throws Exception {
        String src = """
            let nested = ((1, 2), (3, 4))
            print nested
            """;
        assertEquals("((1, 2), (3, 4))", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Tuple Access Methods
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void tupleGet() throws Exception {
        String src = """
            let point = (10, 20, 30)
            print point.get(0)
            print point.get(1)
            print point.get(2)
            """;
        assertEquals("10\n20\n30", run(src));
    }

    @Test
    void tupleFirst() throws Exception {
        assertEquals("1", run("print (1, 2, 3).first()\n"));
    }

    @Test
    void tupleSecond() throws Exception {
        assertEquals("2", run("print (1, 2, 3).second()\n"));
    }

    @Test
    void tupleThird() throws Exception {
        assertEquals("3", run("print (1, 2, 3).third()\n"));
    }

    @Test
    void tupleLast() throws Exception {
        assertEquals("5", run("print (1, 2, 3, 4, 5).last()\n"));
    }

    @Test
    void tupleSize() throws Exception {
        assertEquals("4", run("print (1, 2, 3, 4).size()\n"));
    }

    @Test
    void tupleToList() throws Exception {
        assertEquals("[1, 2, 3]", run("print (1, 2, 3).toList()\n"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Destructuring in Let
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void destructurePair() throws Exception {
        String src = """
            let point = (3, 4)
            let (x, y) = point
            print x
            print y
            """;
        assertEquals("3\n4", run(src));
    }

    @Test
    void destructureTriple() throws Exception {
        String src = """
            let (a, b, c) = (10, 20, 30)
            print a + b + c
            """;
        assertEquals("60", run(src));
    }

    @Test
    void destructureInline() throws Exception {
        String src = """
            let (x, y) = (100, 200)
            print x * y
            """;
        assertEquals("20000", run(src));
    }

    @Test
    void multipleAssignment() throws Exception {
        String src = """
            let a, b = (1, 2)
            print a
            print b
            """;
        assertEquals("1\n2", run(src));
    }

    @Test
    void swapVariables() throws Exception {
        String src = """
            let a = 10
            let b = 20
            let a, b = (b, a)
            print a
            print b
            """;
        assertEquals("20\n10", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Destructuring in For Loops
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void forLoopDestructure() throws Exception {
        String src = """
            let points = [(1, 2), (3, 4), (5, 6)]
            for (x, y) in points:
                print x + y
            """;
        assertEquals("3\n7\n11", run(src));
    }

    @Test
    void forLoopDestructureSum() throws Exception {
        String src = """
            let pairs = [(1, 10), (2, 20), (3, 30)]
            let sum = 0
            for (a, b) in pairs:
                let sum = sum + a + b
            print sum
            """;
        assertEquals("66", run(src));
    }

    @Test
    void forLoopDestructureTriple() throws Exception {
        String src = """
            let data = [(1, 2, 3), (4, 5, 6)]
            for (a, b, c) in data:
                print a * b * c
            """;
        assertEquals("6\n120", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Tuple Pattern Matching
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void matchTupleOrigin() throws Exception {
        String src = """
            let coord = (0, 0)
            match coord:
                (0, 0) -> print "origin"
                _ -> print "other"
            """;
        assertEquals("origin", run(src));
    }

    @Test
    void matchTupleXAxis() throws Exception {
        String src = """
            let coord = (5, 0)
            match coord:
                (0, 0) -> print "origin"
                (x, 0) -> print "x-axis at " + x
                _ -> print "other"
            """;
        assertEquals("x-axis at 5", run(src));
    }

    @Test
    void matchTupleYAxis() throws Exception {
        String src = """
            let coord = (0, 7)
            match coord:
                (0, 0) -> print "origin"
                (x, 0) -> print "x-axis"
                (0, y) -> print "y-axis at " + y
                _ -> print "other"
            """;
        assertEquals("y-axis at 7", run(src));
    }

    @Test
    void matchTupleGeneral() throws Exception {
        String src = """
            let coord = (3, 4)
            match coord:
                (0, 0) -> print "origin"
                (x, 0) -> print "x-axis"
                (0, y) -> print "y-axis"
                (x, y) -> print "at " + x + ", " + y
            """;
        assertEquals("at 3, 4", run(src));
    }

    @Test
    void matchTupleWithWildcard() throws Exception {
        String src = """
            let point = (10, 20)
            match point:
                (_, 0) -> print "on x-axis"
                (0, _) -> print "on y-axis"
                (x, _) -> print "x is " + x
            """;
        assertEquals("x is 10", run(src));
    }

    @Test
    void matchTupleInLoop() throws Exception {
        String src = """
            let points = [(0, 0), (1, 0), (0, 2), (3, 4)]
            for p in points:
                match p:
                    (0, 0) -> print "origin"
                    (x, 0) -> print "x:" + x
                    (0, y) -> print "y:" + y
                    (x, y) -> print x + "," + y
            """;
        assertEquals("origin\nx:1\ny:2\n3,4", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Tuple Equality
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void tupleEquality() throws Exception {
        String src = """
            let a = (1, 2)
            let b = (1, 2)
            print a == b
            """;
        assertEquals("true", run(src));
    }

    @Test
    void tupleInequality() throws Exception {
        String src = """
            let a = (1, 2)
            let b = (1, 3)
            print a == b
            """;
        assertEquals("false", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Practical Examples
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void distanceCalculation() throws Exception {
        String src = """
            let point = (3, 4)
            let (x, y) = point
            print x * x + y * y
            """;
        assertEquals("25", run(src));
    }

    @Test
    void coordinateTransform() throws Exception {
        String src = """
            let points = [(1, 1), (2, 2), (3, 3)]
            for (x, y) in points:
                print (x * 2, y * 2)
            """;
        assertEquals("(2, 2)\n(4, 4)\n(6, 6)", run(src));
    }
}
