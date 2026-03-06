package com.puzzlang;

import com.puzzlang.api.CompiledUnit;
import com.puzzlang.api.PuzzCompiler;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PuzzLang math helpers:
 *   - Global math functions (gcd, lcm, primes_up_to, etc.)
 *   - Integer methods (digits, is_prime, factors, etc.)
 *   - Modulo operator (%)
 */
class MathTest {

    private String run(String source) throws Exception {
        PuzzCompiler compiler = new PuzzCompiler();
        CompiledUnit unit = compiler.compile(source, "MathTest");
        
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
    //  Global Math Functions
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void gcdTwoNumbers() throws Exception {
        assertEquals("4", run("print gcd(12, 8)\n"));
        assertEquals("6", run("print gcd(54, 24)\n"));
        assertEquals("1", run("print gcd(17, 13)\n"));
    }

    @Test
    void gcdMultipleNumbers() throws Exception {
        assertEquals("2", run("print gcd(12, 8, 6)\n"));
        assertEquals("3", run("print gcd(9, 12, 15)\n"));
    }

    @Test
    void lcmTwoNumbers() throws Exception {
        assertEquals("12", run("print lcm(4, 6)\n"));
        assertEquals("36", run("print lcm(12, 18)\n"));
        assertEquals("221", run("print lcm(13, 17)\n"));
    }

    @Test
    void lcmMultipleNumbers() throws Exception {
        assertEquals("60", run("print lcm(3, 4, 5)\n"));
        assertEquals("12", run("print lcm(2, 3, 4)\n"));
    }

    @Test
    void absFunction() throws Exception {
        assertEquals("5", run("print abs(5)\n"));
        assertEquals("5", run("print abs(0 - 5)\n"));
        assertEquals("0", run("print abs(0)\n"));
    }

    @Test
    void signFunction() throws Exception {
        assertEquals("1", run("print sign(42)\n"));
        assertEquals("-1", run("print sign(0 - 7)\n"));
        assertEquals("0", run("print sign(0)\n"));
    }

    @Test
    void minMaxFunctions() throws Exception {
        assertEquals("3", run("print min(5, 3, 8)\n"));
        assertEquals("8", run("print max(5, 3, 8)\n"));
        assertEquals("1", run("print min(1, 2)\n"));
        assertEquals("2", run("print max(1, 2)\n"));
    }

    @Test
    void powFunction() throws Exception {
        assertEquals("8", run("print pow(2, 3)\n"));
        assertEquals("1024", run("print pow(2, 10)\n"));
        assertEquals("1", run("print pow(5, 0)\n"));
        assertEquals("125", run("print pow(5, 3)\n"));
    }

    @Test
    void sqrtFunction() throws Exception {
        assertEquals("3", run("print sqrt(9)\n"));
        assertEquals("10", run("print sqrt(100)\n"));
        assertEquals("4", run("print sqrt(16)\n"));
        assertEquals("4", run("print sqrt(20)\n"));  // floor
    }

    @Test
    void clampFunction() throws Exception {
        assertEquals("5", run("print clamp(3, 5, 10)\n"));  // 3 clamped to [5,10]
        assertEquals("10", run("print clamp(15, 5, 10)\n")); // 15 clamped to [5,10]
        assertEquals("7", run("print clamp(7, 5, 10)\n"));   // 7 in range
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Prime Functions
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void isPrimeFunction() throws Exception {
        assertEquals("true", run("print is_prime(2)\n"));
        assertEquals("true", run("print is_prime(17)\n"));
        assertEquals("true", run("print is_prime(97)\n"));
        assertEquals("false", run("print is_prime(1)\n"));
        assertEquals("false", run("print is_prime(4)\n"));
        assertEquals("false", run("print is_prime(100)\n"));
    }

    @Test
    void primesUpTo() throws Exception {
        assertEquals("[2, 3, 5, 7]", run("print primes_up_to(10)\n"));
        assertEquals("[2, 3, 5, 7, 11, 13, 17, 19, 23, 29]", run("print primes_up_to(30)\n"));
        assertEquals("[]", run("print primes_up_to(1)\n"));
    }

    @Test
    void nthPrime() throws Exception {
        assertEquals("2", run("print nth_prime(1)\n"));
        assertEquals("3", run("print nth_prime(2)\n"));
        assertEquals("5", run("print nth_prime(3)\n"));
        assertEquals("29", run("print nth_prime(10)\n"));
    }

    @Test
    void primeFactors() throws Exception {
        assertEquals("[2, 2, 3]", run("print prime_factors(12)\n"));
        assertEquals("[2, 3, 5]", run("print prime_factors(30)\n"));
        assertEquals("[7]", run("print prime_factors(7)\n"));
        assertEquals("[]", run("print prime_factors(1)\n"));
    }

    @Test
    void divisorsFunction() throws Exception {
        assertEquals("[1, 2, 3, 4, 6, 12]", run("print divisors(12)\n"));
        assertEquals("[1, 2, 4, 5, 10, 20]", run("print divisors(20)\n"));
        assertEquals("[1, 7]", run("print divisors(7)\n"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Digit Functions
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void digitsFunction() throws Exception {
        assertEquals("[1, 2, 3]", run("print digits(123)\n"));
        assertEquals("[0]", run("print digits(0)\n"));
        assertEquals("[9, 8, 7, 6]", run("print digits(9876)\n"));
    }

    @Test
    void digitSumFunction() throws Exception {
        assertEquals("6", run("print digit_sum(123)\n"));
        assertEquals("1", run("print digit_sum(1000)\n"));
        assertEquals("45", run("print digit_sum(123456789)\n"));
    }

    @Test
    void fromDigitsFunction() throws Exception {
        String src = """
            let d = digits(123)
            print from_digits(d)
            """;
        assertEquals("123", run(src));
    }

    @Test
    void reverseDigitsFunction() throws Exception {
        assertEquals("321", run("print reverse_digits(123)\n"));
        assertEquals("1", run("print reverse_digits(100)\n"));
    }

    @Test
    void isPalindromeFunction() throws Exception {
        assertEquals("true", run("print is_palindrome(121)\n"));
        assertEquals("true", run("print is_palindrome(12321)\n"));
        assertEquals("false", run("print is_palindrome(123)\n"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Combinatorics
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void factorialFunction() throws Exception {
        assertEquals("1", run("print factorial(0)\n"));
        assertEquals("1", run("print factorial(1)\n"));
        assertEquals("120", run("print factorial(5)\n"));
        assertEquals("3628800", run("print factorial(10)\n"));
    }

    @Test
    void binomialFunction() throws Exception {
        assertEquals("10", run("print binomial(5, 2)\n"));
        assertEquals("1", run("print binomial(5, 0)\n"));
        assertEquals("1", run("print binomial(5, 5)\n"));
        assertEquals("252", run("print binomial(10, 5)\n"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Modular Arithmetic
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void modFunction() throws Exception {
        assertEquals("2", run("print mod(5, 3)\n"));
        assertEquals("0", run("print mod(6, 3)\n"));
        // Proper modulo for negative numbers
        assertEquals("1", run("print mod(0 - 5, 3)\n"));  // -5 mod 3 = 1
    }

    @Test
    void modPowFunction() throws Exception {
        assertEquals("1", run("print mod_pow(2, 10, 1023)\n"));  // 2^10 = 1024 mod 1023 = 1
        assertEquals("4", run("print mod_pow(2, 10, 20)\n"));    // 2^10 = 1024 mod 20 = 4
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Bit Operations
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void popcountFunction() throws Exception {
        assertEquals("3", run("print popcount(7)\n"));   // 111 in binary
        assertEquals("1", run("print popcount(8)\n"));   // 1000 in binary
        assertEquals("8", run("print popcount(255)\n")); // 11111111 in binary
    }

    @Test
    void bitCountFunction() throws Exception {
        assertEquals("3", run("print bit_count(7)\n"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Base Conversion
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void toBaseFunction() throws Exception {
        assertEquals("1010", run("print to_base(10, 2)\n"));    // 10 in binary
        assertEquals("ff", run("print to_base(255, 16)\n"));    // 255 in hex
        assertEquals("12", run("print to_base(10, 8)\n"));      // 10 in octal
    }

    @Test
    void fromBaseFunction() throws Exception {
        assertEquals("10", run("print from_base(\"1010\", 2)\n"));
        assertEquals("255", run("print from_base(\"ff\", 16)\n"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Modulo Operator (%)
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void moduloOperator() throws Exception {
        assertEquals("2", run("print 5 % 3\n"));
        assertEquals("0", run("print 6 % 3\n"));
        assertEquals("1", run("print 10 % 3\n"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Integer Methods
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void integerDigitsMethod() throws Exception {
        assertEquals("[1, 2, 3]", run("print 123.digits()\n"));
    }

    @Test
    void integerIsPrimeMethod() throws Exception {
        assertEquals("true", run("print 17.is_prime()\n"));
        assertEquals("false", run("print 18.is_prime()\n"));
    }

    @Test
    void integerGcdMethod() throws Exception {
        assertEquals("4", run("print 12.gcd(8)\n"));
    }

    @Test
    void integerLcmMethod() throws Exception {
        assertEquals("12", run("print 4.lcm(6)\n"));
    }

    @Test
    void integerAbsMethod() throws Exception {
        assertEquals("5", run("let n = 0 - 5\nprint n.abs()\n"));
    }

    @Test
    void integerFactorsMethod() throws Exception {
        assertEquals("[1, 2, 3, 4, 6, 12]", run("print 12.factors()\n"));
    }

    @Test
    void integerPrimeFactorsMethod() throws Exception {
        assertEquals("[2, 2, 3]", run("print 12.prime_factors()\n"));
    }

    @Test
    void integerIsEvenOdd() throws Exception {
        assertEquals("true", run("print 4.is_even()\n"));
        assertEquals("false", run("print 5.is_even()\n"));
        assertEquals("true", run("print 5.is_odd()\n"));
        assertEquals("false", run("print 4.is_odd()\n"));
    }

    @Test
    void integerToBinary() throws Exception {
        assertEquals("1010", run("print 10.to_binary()\n"));
    }

    @Test
    void integerToHex() throws Exception {
        assertEquals("ff", run("print 255.to_hex()\n"));
    }

    @Test
    void integerBitCount() throws Exception {
        assertEquals("3", run("print 7.bit_count()\n"));
    }

    @Test
    void integerPowMethod() throws Exception {
        assertEquals("1024", run("print 2.pow(10)\n"));
    }

    @Test
    void integerModMethod() throws Exception {
        assertEquals("2", run("print 5.mod(3)\n"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Integration Tests (AoC-style)
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void sumOfPrimesBelow100() throws Exception {
        String src = """
            let primes = primes_up_to(100)
            print primes.sum()
            """;
        assertEquals("1060", run(src));
    }

    @Test
    void lcmOfRange() throws Exception {
        // LCM of 1..10 = 2520
        String src = """
            let result = 1
            for n in 1..=10:
                let result = lcm(result, n)
            print result
            """;
        assertEquals("2520", run(src));
    }

    @Test
    void countPalindromesUnder1000() throws Exception {
        String src = """
            let count = 0
            for n in 1..1000:
                if is_palindrome(n):
                    let count = count + 1
            print count
            """;
        assertEquals("108", run(src));
    }

    @Test
    void gaussFormula() throws Exception {
        // Sum 1..100 using both range and formula
        String src = """
            print (1..=100).sum()
            """;
        assertEquals("5050", run(src));
    }

    @Test
    void perfectSquaresUnder100() throws Exception {
        String src = """
            let count = 0
            for n in 1..100:
                if is_perfect_square(n):
                    let count = count + 1
            print count
            """;
        assertEquals("9", run(src));  // 1, 4, 9, 16, 25, 36, 49, 64, 81
    }
}
