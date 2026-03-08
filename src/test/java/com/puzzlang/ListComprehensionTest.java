package com.puzzlang;

import com.puzzlang.api.CompiledUnit;
import com.puzzlang.api.PuzzCompiler;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for List Comprehension functionality in PuzzLang:
 *   - Basic: [x * x for x in 1..=10]
 *   - With filter: [x for x in nums if x % 2 == 0]
 *   - Nested: [(x, y) for x in 1..=3 for y in 1..=3]
 *   - With tuple destructuring: [x + y for (x, y) in points]
 *   - List literals: [], [1, 2, 3]
 */
class ListComprehensionTest {

    private String run(String source) throws Exception {
        PuzzCompiler compiler = new PuzzCompiler();
        CompiledUnit unit = compiler.compile(source, "ListCompTest");
        
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
    //  List Literals
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void emptyListLiteral() throws Exception {
        assertEquals("[]", run("print []\n"));
    }

    @Test
    void singleElementList() throws Exception {
        assertEquals("[42]", run("print [42]\n"));
    }

    @Test
    void multiElementList() throws Exception {
        assertEquals("[1, 2, 3]", run("print [1, 2, 3]\n"));
    }

    @Test
    void listWithExpressions() throws Exception {
        String src = """
            let x = 10
            print [x, x + 1, x + 2]
            """;
        assertEquals("[10, 11, 12]", run(src));
    }

    @Test
    void nestedLists() throws Exception {
        assertEquals("[[1, 2], [3, 4]]", run("print [[1, 2], [3, 4]]\n"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Basic List Comprehensions
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void squaresComprehension() throws Exception {
        assertEquals("[1, 4, 9, 16, 25]", run("print [x * x for x in 1..=5]\n"));
    }

    @Test
    void doubleComprehension() throws Exception {
        assertEquals("[2, 4, 6, 8, 10]", run("print [x * 2 for x in 1..=5]\n"));
    }

    @Test
    void identityComprehension() throws Exception {
        assertEquals("[1, 2, 3, 4, 5]", run("print [x for x in 1..=5]\n"));
    }

    @Test
    void comprehensionWithVariable() throws Exception {
        String src = """
            let nums = [10, 20, 30]
            print [n + 1 for n in nums]
            """;
        assertEquals("[11, 21, 31]", run(src));
    }

    @Test
    void comprehensionWithExpression() throws Exception {
        assertEquals("[0, 1, 4, 9, 16]", run("print [x * x for x in 0..5]\n"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  List Comprehensions with Filter
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void filterEvens() throws Exception {
        assertEquals("[2, 4, 6, 8, 10]", run("print [x for x in 1..=10 if x % 2 == 0]\n"));
    }

    @Test
    void filterOdds() throws Exception {
        assertEquals("[1, 3, 5, 7, 9]", run("print [x for x in 1..=10 if x % 2 != 0]\n"));
    }

    @Test
    void filterGreaterThan() throws Exception {
        assertEquals("[6, 7, 8, 9, 10]", run("print [x for x in 1..=10 if x > 5]\n"));
    }

    @Test
    void filterAndTransform() throws Exception {
        assertEquals("[4, 16, 36, 64, 100]", 
                run("print [x * x for x in 1..=10 if x % 2 == 0]\n"));
    }

    @Test
    void filterWithVariable() throws Exception {
        String src = """
            let threshold = 5
            let nums = [1, 3, 5, 7, 9]
            print [n for n in nums if n > threshold]
            """;
        assertEquals("[7, 9]", run(src));
    }

    @Test
    void filterPrimes() throws Exception {
        String src = """
            let primes = [x for x in 2..20 if is_prime(x)]
            print primes
            """;
        assertEquals("[2, 3, 5, 7, 11, 13, 17, 19]", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Nested List Comprehensions
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void nestedPairs() throws Exception {
        String src = "print [(x, y) for x in 1..=2 for y in 1..=2]\n";
        assertEquals("[(1, 1), (1, 2), (2, 1), (2, 2)]", run(src));
    }

    @Test
    void nestedTriple() throws Exception {
        String src = "print [(x, y) for x in 1..=3 for y in 1..=3]\n";
        String expected = "[(1, 1), (1, 2), (1, 3), (2, 1), (2, 2), (2, 3), (3, 1), (3, 2), (3, 3)]";
        assertEquals(expected, run(src));
    }

    @Test
    void nestedSums() throws Exception {
        String src = "print [x + y for x in 1..=2 for y in 10..=12]\n";
        assertEquals("[11, 12, 13, 12, 13, 14]", run(src));
    }

    @Test
    void nestedProducts() throws Exception {
        String src = "print [x * y for x in 1..=3 for y in 1..=3]\n";
        assertEquals("[1, 2, 3, 2, 4, 6, 3, 6, 9]", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Comprehensions with Tuple Destructuring
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void destructureInComprehension() throws Exception {
        String src = """
            let points = [(1, 2), (3, 4), (5, 6)]
            print [x + y for (x, y) in points]
            """;
        assertEquals("[3, 7, 11]", run(src));
    }

    @Test
    void destructureAndTransform() throws Exception {
        String src = """
            let points = [(1, 2), (3, 4)]
            print [(y, x) for (x, y) in points]
            """;
        assertEquals("[(2, 1), (4, 3)]", run(src));
    }

    @Test
    void destructureWithFilter() throws Exception {
        String src = """
            let points = [(0, 0), (1, 0), (0, 2), (3, 4)]
            print [(x, y) for (x, y) in points if x > 0]
            """;
        assertEquals("[(1, 0), (3, 4)]", run(src));
    }

    @Test
    void distancesComprehension() throws Exception {
        String src = """
            let points = [(3, 4), (5, 12), (8, 15)]
            print [x * x + y * y for (x, y) in points]
            """;
        assertEquals("[25, 169, 289]", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Comprehension Results as Values
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void comprehensionSum() throws Exception {
        assertEquals("55", run("print [x for x in 1..=10].sum()\n"));
    }

    @Test
    void comprehensionCount() throws Exception {
        assertEquals("5", run("print [x for x in 1..=10 if x % 2 == 0].count()\n"));
    }

    @Test
    void comprehensionMax() throws Exception {
        assertEquals("100", run("print [x * x for x in 1..=10].max()\n"));
    }

    @Test
    void comprehensionFirst() throws Exception {
        assertEquals("4", run("print [x * x for x in 2..=5].first()\n"));
    }

    @Test
    void chainedComprehension() throws Exception {
        String src = "print [x * x for x in 1..=5].reversed()\n";
        assertEquals("[25, 16, 9, 4, 1]", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Practical Examples
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void fibonacciSquares() throws Exception {
        String src = """
            let fibs = [1, 1, 2, 3, 5, 8]
            print [f * f for f in fibs]
            """;
        assertEquals("[1, 1, 4, 9, 25, 64]", run(src));
    }

    @Test
    void pythagoreanTriples() throws Exception {
        // Find all (a, b) pairs where a^2 + b^2 = 25 (i.e., 3,4 and 4,3)
        String src = """
            let pairs = [(a, b) for a in 1..=4 for b in 1..=4]
            let pythagorean = [(a, b) for (a, b) in pairs if a * a + b * b == 25]
            print pythagorean
            """;
        assertEquals("[(3, 4), (4, 3)]", run(src));
    }

    @Test
    void multiplicationTable() throws Exception {
        String src = "print [x * y for x in 1..=3 for y in 1..=3]\n";
        assertEquals("[1, 2, 3, 2, 4, 6, 3, 6, 9]", run(src));
    }

    @Test
    void coordinateGrid() throws Exception {
        String src = "print [(x, y) for x in 0..=1 for y in 0..=1]\n";
        assertEquals("[(0, 0), (0, 1), (1, 0), (1, 1)]", run(src));
    }
}
