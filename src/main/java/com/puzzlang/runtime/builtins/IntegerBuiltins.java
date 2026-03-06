package com.puzzlang.runtime.builtins;

import com.puzzlang.runtime.*;

/**
 * Built-in methods for Integer types.
 *
 * Registers all integer-related methods with the MethodRegistry.
 *
 * SUPPORTED METHODS:
 *
 *   Digit operations:
 *     n.digits()           → [1, 2, 3] for 123
 *     n.digit_sum()        → sum of digits
 *     n.digit_count()      → number of digits
 *     n.reverse_digits()   → 321 for 123
 *     n.is_palindrome()    → true if digits are palindromic
 *
 *   Primality:
 *     n.is_prime()         → true if prime
 *     n.prime_factors()    → [2, 2, 3] for 12
 *     n.factors()          → [1, 2, 3, 4, 6, 12] for 12
 *
 *   Arithmetic:
 *     n.abs()              → absolute value
 *     n.sign()             → -1, 0, or 1
 *     n.gcd(m)             → greatest common divisor
 *     n.lcm(m)             → least common multiple
 *     n.pow(exp)           → n^exp
 *     n.mod(m)             → proper modulo (always positive)
 *     n.divmod(m)          → [quotient, remainder]
 *
 *   Bit operations:
 *     n.bit_count()        → number of 1 bits (popcount)
 *     n.highest_bit()      → position of highest set bit
 *     n.lowest_bit()       → position of lowest set bit
 *
 *   Predicates:
 *     n.is_even()          → true if even
 *     n.is_odd()           → true if odd
 *     n.is_positive()      → true if > 0
 *     n.is_negative()      → true if < 0
 *     n.is_zero()          → true if == 0
 *     n.is_perfect_square()→ true if perfect square
 *
 *   Conversion:
 *     n.to_base(b)         → string representation in base b
 *     n.to_binary()        → binary string
 *     n.to_hex()           → hex string
 */
public class IntegerBuiltins {

    private IntegerBuiltins() {} // Static utility class

    /**
     * Registers all integer methods with the given registry.
     */
    public static void register(MethodRegistry registry) {
        Class<Integer> intType = Integer.class;
        Class<Long> longType = Long.class;

        // Register for both Integer and Long types
        registerMethods(registry, intType);
        registerMethods(registry, longType);
    }

    private static void registerMethods(MethodRegistry registry, Class<? extends Number> type) {
        
        // ── Digit operations ────────────────────────────────────────────────

        registry.register(type, "digits", (r, args) -> {
            requireArgs("digits", args, 0);
            return PuzzMath.digits(toLong(r));
        });

        registry.register(type, "digit_sum", (r, args) -> {
            requireArgs("digit_sum", args, 0);
            return PuzzMath.digitSum(toLong(r));
        });

        registry.register(type, "digit_count", (r, args) -> {
            requireArgs("digit_count", args, 0);
            return PuzzMath.digitCount(toLong(r));
        });

        registry.register(type, "reverse_digits", (r, args) -> {
            requireArgs("reverse_digits", args, 0);
            return PuzzMath.reverseDigits(toLong(r));
        });

        registry.register(type, "is_palindrome", (r, args) -> {
            requireArgs("is_palindrome", args, 0);
            return PuzzMath.isPalindrome(toLong(r));
        });

        // ── Primality ───────────────────────────────────────────────────────

        registry.register(type, "is_prime", (r, args) -> {
            requireArgs("is_prime", args, 0);
            return PuzzMath.isPrime(toLong(r));
        });

        registry.register(type, "prime_factors", (r, args) -> {
            requireArgs("prime_factors", args, 0);
            return PuzzMath.primeFactors(toLong(r));
        });

        registry.register(type, "factors", (r, args) -> {
            requireArgs("factors", args, 0);
            return PuzzMath.divisors(toLong(r));
        });

        // Alias for factors
        registry.register(type, "divisors", (r, args) -> {
            requireArgs("divisors", args, 0);
            return PuzzMath.divisors(toLong(r));
        });

        // ── Arithmetic ──────────────────────────────────────────────────────

        registry.register(type, "abs", (r, args) -> {
            requireArgs("abs", args, 0);
            return PuzzMath.abs(toLong(r));
        });

        registry.register(type, "sign", (r, args) -> {
            requireArgs("sign", args, 0);
            return PuzzMath.sign(toLong(r));
        });

        registry.register(type, "gcd", (r, args) -> {
            requireArgs("gcd", args, 1);
            return PuzzMath.gcd(toLong(r), toLong(args[0]));
        });

        registry.register(type, "lcm", (r, args) -> {
            requireArgs("lcm", args, 1);
            return PuzzMath.lcm(toLong(r), toLong(args[0]));
        });

        registry.register(type, "pow", (r, args) -> {
            requireArgs("pow", args, 1);
            return PuzzMath.pow(toLong(r), toInt(args[0]));
        });

        registry.register(type, "mod", (r, args) -> {
            requireArgs("mod", args, 1);
            return PuzzMath.mod(toLong(r), toLong(args[0]));
        });

        registry.register(type, "divmod", (r, args) -> {
            requireArgs("divmod", args, 1);
            return PuzzMath.divmod(toLong(r), toLong(args[0]));
        });

        registry.register(type, "sqrt", (r, args) -> {
            requireArgs("sqrt", args, 0);
            return PuzzMath.isqrt(toLong(r));
        });

        registry.register(type, "isqrt", (r, args) -> {
            requireArgs("isqrt", args, 0);
            return PuzzMath.isqrt(toLong(r));
        });

        // ── Bit operations ──────────────────────────────────────────────────

        registry.register(type, "bit_count", (r, args) -> {
            requireArgs("bit_count", args, 0);
            return PuzzMath.popcount(toLong(r));
        });

        registry.register(type, "popcount", (r, args) -> {
            requireArgs("popcount", args, 0);
            return PuzzMath.popcount(toLong(r));
        });

        registry.register(type, "highest_bit", (r, args) -> {
            requireArgs("highest_bit", args, 0);
            return PuzzMath.highestBit(toLong(r));
        });

        registry.register(type, "lowest_bit", (r, args) -> {
            requireArgs("lowest_bit", args, 0);
            return PuzzMath.lowestBit(toLong(r));
        });

        // ── Predicates ──────────────────────────────────────────────────────

        registry.register(type, "is_even", (r, args) -> {
            requireArgs("is_even", args, 0);
            return toLong(r) % 2 == 0;
        });

        registry.register(type, "is_odd", (r, args) -> {
            requireArgs("is_odd", args, 0);
            return toLong(r) % 2 != 0;
        });

        registry.register(type, "is_positive", (r, args) -> {
            requireArgs("is_positive", args, 0);
            return toLong(r) > 0;
        });

        registry.register(type, "is_negative", (r, args) -> {
            requireArgs("is_negative", args, 0);
            return toLong(r) < 0;
        });

        registry.register(type, "is_zero", (r, args) -> {
            requireArgs("is_zero", args, 0);
            return toLong(r) == 0;
        });

        registry.register(type, "is_perfect_square", (r, args) -> {
            requireArgs("is_perfect_square", args, 0);
            return PuzzMath.isPerfectSquare(toLong(r));
        });

        // ── Base conversion ─────────────────────────────────────────────────

        registry.register(type, "to_base", (r, args) -> {
            requireArgs("to_base", args, 1);
            int base = toInt(args[0]);
            if (base < 2 || base > 36) {
                throw new PuzzLangException("to_base requires base between 2 and 36, got %d", base);
            }
            return Long.toString(toLong(r), base);
        });

        registry.register(type, "to_binary", (r, args) -> {
            requireArgs("to_binary", args, 0);
            return Long.toBinaryString(toLong(r));
        });

        registry.register(type, "to_hex", (r, args) -> {
            requireArgs("to_hex", args, 0);
            return Long.toHexString(toLong(r));
        });

        registry.register(type, "to_octal", (r, args) -> {
            requireArgs("to_octal", args, 0);
            return Long.toOctalString(toLong(r));
        });

        // ── Clamping ────────────────────────────────────────────────────────

        registry.register(type, "clamp", (r, args) -> {
            requireArgs("clamp", args, 2);
            return PuzzMath.clamp(toLong(r), toLong(args[0]), toLong(args[1]));
        });

        // ── Modular arithmetic ──────────────────────────────────────────────

        registry.register(type, "mod_pow", (r, args) -> {
            requireArgs("mod_pow", args, 2);
            return PuzzMath.modPow(toLong(r), toLong(args[0]), toLong(args[1]));
        });

        registry.register(type, "mod_inverse", (r, args) -> {
            requireArgs("mod_inverse", args, 1);
            return PuzzMath.modInverse(toLong(r), toLong(args[0]));
        });
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private static void requireArgs(String method, Object[] args, int expected) {
        if (args.length != expected) {
            throw PuzzLangException.argumentCount(method, expected, args.length);
        }
    }

    private static long toLong(Object obj) {
        if (obj instanceof Long l) return l;
        if (obj instanceof Integer i) return i.longValue();
        if (obj instanceof Number n) return n.longValue();
        throw new PuzzLangException("expected integer, got %s", obj.getClass().getSimpleName());
    }

    private static int toInt(Object obj) {
        if (obj instanceof Integer i) return i;
        if (obj instanceof Long l) return l.intValue();
        if (obj instanceof Number n) return n.intValue();
        throw new PuzzLangException("expected integer, got %s", obj.getClass().getSimpleName());
    }
}
