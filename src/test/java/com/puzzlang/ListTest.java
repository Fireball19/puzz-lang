package com.puzzlang;

import com.puzzlang.api.CompiledUnit;
import com.puzzlang.api.PuzzCompiler;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PuzzList operations:
 *   - Access methods (count, first, last, get, isEmpty)
 *   - Transformation methods (join, reversed, sorted, unique)
 *   - Aggregation methods (sum, product, min, max)
 *   - Search methods (contains, indexOf)
 *   - Iteration (for-in loops)
 */
class ListTest {

    private String run(String source) throws Exception {
        PuzzCompiler compiler = new PuzzCompiler();
        CompiledUnit unit = compiler.compile(source, "ListTest");
        
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
    //  List Creation (from various sources)
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void listFromDigits() throws Exception {
        assertEquals("[1, 2, 3, 4, 5]", run("print 12345.digits()\n"));
    }

    @Test
    void listFromPrimes() throws Exception {
        assertEquals("[2, 3, 5, 7]", run("print primes_up_to(10)\n"));
    }

    @Test
    void listFromFactors() throws Exception {
        assertEquals("[1, 2, 3, 4, 6, 12]", run("print 12.factors()\n"));
    }

    @Test
    void listFromPrimeFactors() throws Exception {
        assertEquals("[2, 2, 3]", run("print 12.prime_factors()\n"));
    }

    @Test
    void listFromRangeToList() throws Exception {
        assertEquals("[1, 2, 3, 4, 5]", run("print (1..=5).toList()\n"));
    }

    @Test
    void listFromStringLines() throws Exception {
        // Use \n escape sequence for newlines within the string
        String src = "let text = \"a\\nb\\nc\"\nprint text.lines().count()\n";
        assertEquals("3", run(src));
    }

    @Test
    void listFromStringSplit() throws Exception {
        String src = """
            let parts = "a,b,c".split(",")
            print parts.count()
            """;
        assertEquals("3", run(src));
    }

    @Test
    void listFromStringChars() throws Exception {
        String src = """
            let chars = "hello".chars()
            print chars.count()
            """;
        assertEquals("5", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Access Methods
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void listCount() throws Exception {
        assertEquals("5", run("print 12345.digits().count()\n"));
    }

    @Test
    void listLength() throws Exception {
        assertEquals("5", run("print 12345.digits().length()\n"));
    }

    @Test
    void listSize() throws Exception {
        assertEquals("5", run("print 12345.digits().size()\n"));
    }

    @Test
    void listFirst() throws Exception {
        assertEquals("1", run("print 12345.digits().first()\n"));
    }

    @Test
    void listLast() throws Exception {
        assertEquals("5", run("print 12345.digits().last()\n"));
    }

    @Test
    void listGet() throws Exception {
        String src = """
            let d = 12345.digits()
            print d.get(0)
            print d.get(2)
            print d.get(4)
            """;
        assertEquals("1\n3\n5", run(src));
    }

    @Test
    void listIsEmpty() throws Exception {
        assertEquals("false", run("print 123.digits().isEmpty()\n"));
        assertEquals("false", run("print 123.digits().is_empty()\n"));
    }

    @Test
    void listIsEmptyTrue() throws Exception {
        assertEquals("true", run("print primes_up_to(1).isEmpty()\n"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Transformation Methods
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void listJoin() throws Exception {
        assertEquals("1-2-3", run("print 123.digits().join(\"-\")\n"));
    }

    @Test
    void listJoinEmpty() throws Exception {
        assertEquals("123", run("print 123.digits().join(\"\")\n"));
    }

    @Test
    void listJoinComma() throws Exception {
        assertEquals("1, 2, 3, 4, 5", run("print 12345.digits().join(\", \")\n"));
    }

    @Test
    void listReversed() throws Exception {
        assertEquals("[5, 4, 3, 2, 1]", run("print 12345.digits().reversed()\n"));
    }

    @Test
    void listReversedDoesNotMutate() throws Exception {
        String src = """
            let original = 12345.digits()
            let rev = original.reversed()
            print original
            print rev
            """;
        assertEquals("[1, 2, 3, 4, 5]\n[5, 4, 3, 2, 1]", run(src));
    }

    @Test
    void listSorted() throws Exception {
        assertEquals("[1, 2, 3, 4, 5]", run("print 31524.digits().sorted()\n"));
    }

    @Test
    void listSortedAlreadySorted() throws Exception {
        assertEquals("[1, 2, 3, 4, 5]", run("print 12345.digits().sorted()\n"));
    }

    @Test
    void listSortedReverse() throws Exception {
        assertEquals("[1, 2, 3, 4, 5]", run("print 54321.digits().sorted()\n"));
    }

    @Test
    void listSortedDoesNotMutate() throws Exception {
        String src = """
            let original = 31524.digits()
            let s = original.sorted()
            print original
            print s
            """;
        assertEquals("[3, 1, 5, 2, 4]\n[1, 2, 3, 4, 5]", run(src));
    }

    @Test
    void listUnique() throws Exception {
        assertEquals("[1, 2, 3]", run("print 112233.digits().unique()\n"));
    }

    @Test
    void listUniquePreservesOrder() throws Exception {
        assertEquals("[3, 1, 2]", run("print 311223.digits().unique()\n"));
    }

    @Test
    void listUniqueAlreadyUnique() throws Exception {
        assertEquals("[1, 2, 3, 4, 5]", run("print 12345.digits().unique()\n"));
    }

    @Test
    void listUniqueDoesNotMutate() throws Exception {
        String src = """
            let original = 112233.digits()
            let u = original.unique()
            print original
            print u
            """;
        assertEquals("[1, 1, 2, 2, 3, 3]\n[1, 2, 3]", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Aggregation Methods
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void listSum() throws Exception {
        assertEquals("15", run("print 12345.digits().sum()\n"));
    }

    @Test
    void listSumPrimes() throws Exception {
        assertEquals("17", run("print primes_up_to(10).sum()\n"));  // 2+3+5+7=17
    }

    @Test
    void listProduct() throws Exception {
        assertEquals("120", run("print 12345.digits().product()\n"));  // 1*2*3*4*5=120
    }

    @Test
    void listProductWithZero() throws Exception {
        assertEquals("0", run("print 12340.digits().product()\n"));
    }

    @Test
    void listMin() throws Exception {
        assertEquals("1", run("print 31524.digits().min()\n"));
    }

    @Test
    void listMinAlreadyFirst() throws Exception {
        assertEquals("1", run("print 12345.digits().min()\n"));
    }

    @Test
    void listMax() throws Exception {
        assertEquals("5", run("print 31524.digits().max()\n"));
    }

    @Test
    void listMaxAlreadyFirst() throws Exception {
        assertEquals("9", run("print 98765.digits().max()\n"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Search Methods
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void listContainsTrue() throws Exception {
        assertEquals("true", run("print 12345.digits().contains(3)\n"));
    }

    @Test
    void listCountOccurrences() throws Exception {
        assertEquals("2", run("print 11234.digits().count(1)\n"));
    }

    @Test
    void listCountOccurrencesNone() throws Exception {
        assertEquals("0", run("print 12345.digits().count(9)\n"));
    }

    @Test
    void listCountOccurrencesAll() throws Exception {
        assertEquals("5", run("print 11111.digits().count(1)\n"));
    }

    @Test
    void listCountNoArgsReturnsSize() throws Exception {
        assertEquals("5", run("print 12345.digits().count()\n"));
    }

    @Test
    void listContainsFalse() throws Exception {
        assertEquals("false", run("print 12345.digits().contains(9)\n"));
    }

    @Test
    void listIndexOf() throws Exception {
        assertEquals("2", run("print 12345.digits().indexOf(3)\n"));
    }

    @Test
    void listIndexOfSnakeCase() throws Exception {
        assertEquals("2", run("print 12345.digits().index_of(3)\n"));
    }

    @Test
    void listIndexOfNotFound() throws Exception {
        assertEquals("-1", run("print 12345.digits().indexOf(9)\n"));
    }

    @Test
    void listIndexOfFirst() throws Exception {
        assertEquals("0", run("print 12345.digits().indexOf(1)\n"));
    }

    @Test
    void listIndexOfLast() throws Exception {
        assertEquals("4", run("print 12345.digits().indexOf(5)\n"));
    }

    @Test
    void listIndexOfDuplicates() throws Exception {
        // Should return first occurrence
        assertEquals("0", run("print 11234.digits().indexOf(1)\n"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Iteration
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void listForInLoop() throws Exception {
        String src = """
            let sum = 0
            for x in 12345.digits():
                let sum = sum + x
            print sum
            """;
        assertEquals("15", run(src));
    }

    @Test
    void listForInWithPrimes() throws Exception {
        String src = """
            let count = 0
            for p in primes_up_to(20):
                let count = count + 1
            print count
            """;
        assertEquals("8", run(src));  // 2,3,5,7,11,13,17,19
    }

    @Test
    void listForInWithFactors() throws Exception {
        String src = """
            let sum = 0
            for f in 6.factors():
                let sum = sum + f
            print sum
            """;
        assertEquals("12", run(src));  // 1+2+3+6=12
    }

    @Test
    void listForInNested() throws Exception {
        String src = """
            let count = 0
            for a in 12.digits():
                for b in 34.digits():
                    let count = count + 1
            print count
            """;
        assertEquals("4", run(src));  // 2 * 2 = 4
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Chained Operations
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void listChainReversedSorted() throws Exception {
        // Reverse then sort should give sorted order
        assertEquals("[1, 2, 3, 4, 5]", run("print 54321.digits().reversed().sorted()\n"));
    }

    @Test
    void listChainSortedReversed() throws Exception {
        // Sort then reverse should give descending order
        assertEquals("[5, 4, 3, 2, 1]", run("print 31524.digits().sorted().reversed()\n"));
    }

    @Test
    void listChainUniqueSum() throws Exception {
        assertEquals("6", run("print 112233.digits().unique().sum()\n"));  // 1+2+3=6
    }

    @Test
    void listChainSortedFirst() throws Exception {
        assertEquals("1", run("print 54321.digits().sorted().first()\n"));
    }

    @Test
    void listChainSortedLast() throws Exception {
        assertEquals("5", run("print 12345.digits().sorted().last()\n"));
    }

    @Test
    void listChainUniqueSortedJoin() throws Exception {
        assertEquals("1-2-3", run("print 321123.digits().unique().sorted().join(\"-\")\n"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Edge Cases
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void listSingleElement() throws Exception {
        String src = """
            let d = 5.digits()
            print d.count()
            print d.first()
            print d.last()
            print d.sum()
            print d.min()
            print d.max()
            """;
        assertEquals("1\n5\n5\n5\n5\n5", run(src));
    }

    @Test
    void listSingleElementReversed() throws Exception {
        assertEquals("[5]", run("print 5.digits().reversed()\n"));
    }

    @Test
    void listSingleElementSorted() throws Exception {
        assertEquals("[5]", run("print 5.digits().sorted()\n"));
    }

    @Test
    void listAllSameElements() throws Exception {
        String src = """
            let d = 11111.digits()
            print d.sum()
            print d.product()
            print d.min()
            print d.max()
            print d.unique()
            """;
        assertEquals("5\n1\n1\n1\n[1]", run(src));
    }

    @Test
    void listWithZeros() throws Exception {
        String src = """
            let d = 10203.digits()
            print d
            print d.sum()
            print d.contains(0)
            """;
        assertEquals("[1, 0, 2, 0, 3]\n6\ntrue", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  String count method
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void stringCountChar() throws Exception {
        assertEquals("3", run("print \"banana\".count(\"a\")\n"));
    }

    @Test
    void stringCountSubstring() throws Exception {
        assertEquals("2", run("print \"abcabc\".count(\"bc\")\n"));
    }

    @Test
    void stringCountNone() throws Exception {
        assertEquals("0", run("print \"hello\".count(\"x\")\n"));
    }

    @Test
    void stringCountParens() throws Exception {
        // AoC 2015 Day 1 style
        assertEquals("3", run("print \"((()))\".count(\"(\")\n"));
        assertEquals("3", run("print \"((()))\".count(\")\")\n"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  String List Operations
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void stringListFromSplit() throws Exception {
        String src = """
            let parts = "apple,banana,cherry".split(",")
            print parts.count()
            print parts.first()
            print parts.last()
            """;
        assertEquals("3\napple\ncherry", run(src));
    }

    @Test
    void stringListJoin() throws Exception {
        String src = """
            let parts = "a,b,c".split(",")
            print parts.join(" - ")
            """;
        assertEquals("a - b - c", run(src));
    }

    @Test
    void stringListReversed() throws Exception {
        String src = """
            let parts = "a,b,c".split(",")
            print parts.reversed().join(",")
            """;
        assertEquals("c,b,a", run(src));
    }

    @Test
    void stringListContains() throws Exception {
        String src = """
            let parts = "apple,banana,cherry".split(",")
            print parts.contains("banana")
            print parts.contains("grape")
            """;
        assertEquals("true\nfalse", run(src));
    }

    @Test
    void stringListIndexOf() throws Exception {
        String src = """
            let parts = "a,b,c,b,d".split(",")
            print parts.indexOf("b")
            print parts.indexOf("d")
            print parts.indexOf("z")
            """;
        assertEquals("1\n4\n-1", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Integration Tests (AoC-style)
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void findMostCommonDigit() throws Exception {
        // Find the digit that appears most in a number
        // 1223334444 -> 4 appears 4 times
        String src = """
            let d = 1223334444.digits()
            let maxCount = 0
            let maxDigit = 0
            for digit in d.unique():
                let count = 0
                for x in d:
                    if x == digit:
                        let count = count + 1
                if count > maxCount:
                    let maxCount = count
                    let maxDigit = digit
            print maxDigit
            """;
        assertEquals("4", run(src));
    }

    @Test
    void sumOfUniqueFactors() throws Exception {
        // Perfect number check: 6 = 1+2+3 (sum of proper divisors)
        String src = """
            let n = 6
            let factors = n.factors()
            let sum = 0
            for f in factors:
                if f < n:
                    let sum = sum + f
            print sum
            """;
        assertEquals("6", run(src));
    }

    @Test
    void countTwinPrimes() throws Exception {
        // Count twin prime pairs (p, p+2) below 50
        String src = """
            let primes = primes_up_to(50)
            let count = 0
            for i in 0..primes.count():
                let p = primes.get(i)
                if primes.contains(p + 2):
                    let count = count + 1
            print count
            """;
        assertEquals("6", run(src));  // (3,5), (5,7), (11,13), (17,19), (29,31), (41,43)
    }

    @Test
    void digitFrequencyAnalysis() throws Exception {
        // Count how many unique digits in a number
        String src = """
            let d = 1234567890.digits()
            print d.unique().count()
            """;
        assertEquals("10", run(src));
    }

    @Test
    void sortedPrimeFactors() throws Exception {
        String src = """
            let factors = 360.prime_factors()
            print factors.sorted()
            """;
        assertEquals("[2, 2, 2, 3, 3, 5]", run(src));
    }

    @Test
    void sumOfSquaresOfDigits() throws Exception {
        // Used in happy number detection
        String src = """
            let n = 19
            let sum = 0
            for d in n.digits():
                let sum = sum + d * d
            print sum
            """;
        assertEquals("82", run(src));  // 1^2 + 9^2 = 1 + 81 = 82
    }
}
