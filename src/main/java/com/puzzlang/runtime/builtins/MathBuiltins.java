package com.puzzlang.runtime.builtins;

import com.puzzlang.runtime.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Built-in global math functions for PuzzLang.
 *
 * These are called as global functions (not methods on objects):
 *   gcd(a, b)         → greatest common divisor
 *   lcm(a, b)         → least common multiple
 *   abs(n)            → absolute value
 *   min(a, b, ...)    → minimum value
 *   max(a, b, ...)    → maximum value
 *   pow(base, exp)    → exponentiation
 *   sqrt(n)           → integer square root
 *   sign(n)           → -1, 0, or 1
 *   clamp(n, lo, hi)  → clamp to range
 *
 * Number theory:
 *   primes_up_to(n)   → list of primes ≤ n
 *   nth_prime(n)      → the n-th prime (1-indexed)
 *   is_prime(n)       → primality test
 *   factors(n)        → all divisors of n
 *   prime_factors(n)  → prime factorization
 *   divisors(n)       → alias for factors
 *
 * Digit operations:
 *   digits(n)         → list of digits
 *   digit_sum(n)      → sum of digits
 *   from_digits(list) → number from digit list
 *
 * Combinatorics:
 *   factorial(n)      → n!
 *   binomial(n, k)    → C(n, k)
 *
 * Modular arithmetic:
 *   mod(a, m)         → proper modulo (always positive)
 *   mod_pow(b, e, m)  → (b^e) mod m
 *   mod_inverse(a, m) → modular multiplicative inverse
 *
 * Bit operations:
 *   popcount(n)       → number of 1 bits
 *   bit_count(n)      → alias for popcount
 */
public class MathBuiltins {

    private MathBuiltins() {} // Static utility class

    /**
     * Functional interface for global function handlers.
     */
    @FunctionalInterface
    public interface FunctionHandler {
        Object invoke(Object[] args);
    }

    // Registry of global functions
    private static final Map<String, FunctionHandler> functions = new HashMap<>();

    static {
        registerAllFunctions();
    }

    /**
     * Dispatches a global function call.
     *
     * @param name The function name
     * @param args The arguments
     * @return The result
     * @throws PuzzLangException if function is unknown or args are invalid
     */
    public static Object call(String name, Object[] args) {
        FunctionHandler handler = functions.get(name);
        if (handler == null) {
            throw new PuzzLangException("unknown function '%s'", name);
        }
        return handler.invoke(args);
    }

    /**
     * Checks if a function exists.
     */
    public static boolean hasFunction(String name) {
        return functions.containsKey(name);
    }

    /**
     * Gets all registered function names.
     */
    public static java.util.Set<String> getFunctionNames() {
        return java.util.Collections.unmodifiableSet(functions.keySet());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Registration
    // ═══════════════════════════════════════════════════════════════════════════

    private static void registerAllFunctions() {
        // ── Basic arithmetic ────────────────────────────────────────────────

        functions.put("abs", args -> {
            requireArgs("abs", args, 1);
            return PuzzMath.abs(toLong(args[0]));
        });

        functions.put("sign", args -> {
            requireArgs("sign", args, 1);
            return PuzzMath.sign(toLong(args[0]));
        });

        functions.put("min", args -> {
            requireMinArgs("min", args, 1);
            long result = toLong(args[0]);
            for (int i = 1; i < args.length; i++) {
                result = Math.min(result, toLong(args[i]));
            }
            return result;
        });

        functions.put("max", args -> {
            requireMinArgs("max", args, 1);
            long result = toLong(args[0]);
            for (int i = 1; i < args.length; i++) {
                result = Math.max(result, toLong(args[i]));
            }
            return result;
        });

        functions.put("pow", args -> {
            requireArgs("pow", args, 2);
            return PuzzMath.pow(toLong(args[0]), toInt(args[1]));
        });

        functions.put("sqrt", args -> {
            requireArgs("sqrt", args, 1);
            return PuzzMath.isqrt(toLong(args[0]));
        });

        functions.put("isqrt", args -> {
            requireArgs("isqrt", args, 1);
            return PuzzMath.isqrt(toLong(args[0]));
        });

        functions.put("clamp", args -> {
            requireArgs("clamp", args, 3);
            return PuzzMath.clamp(toLong(args[0]), toLong(args[1]), toLong(args[2]));
        });

        // ── GCD / LCM ───────────────────────────────────────────────────────

        functions.put("gcd", args -> {
            requireMinArgs("gcd", args, 2);
            long[] values = new long[args.length];
            for (int i = 0; i < args.length; i++) {
                values[i] = toLong(args[i]);
            }
            return PuzzMath.gcd(values);
        });

        functions.put("lcm", args -> {
            requireMinArgs("lcm", args, 2);
            long[] values = new long[args.length];
            for (int i = 0; i < args.length; i++) {
                values[i] = toLong(args[i]);
            }
            return PuzzMath.lcm(values);
        });

        // ── Number theory ───────────────────────────────────────────────────

        functions.put("primes_up_to", args -> {
            requireArgs("primes_up_to", args, 1);
            return PuzzMath.primesUpTo(toInt(args[0]));
        });

        functions.put("nth_prime", args -> {
            requireArgs("nth_prime", args, 1);
            return PuzzMath.nthPrime(toInt(args[0]));
        });

        functions.put("is_prime", args -> {
            requireArgs("is_prime", args, 1);
            return PuzzMath.isPrime(toLong(args[0]));
        });

        functions.put("factors", args -> {
            requireArgs("factors", args, 1);
            return PuzzMath.divisors(toLong(args[0]));
        });

        functions.put("divisors", args -> {
            requireArgs("divisors", args, 1);
            return PuzzMath.divisors(toLong(args[0]));
        });

        functions.put("prime_factors", args -> {
            requireArgs("prime_factors", args, 1);
            return PuzzMath.primeFactors(toLong(args[0]));
        });

        // ── Digit operations ────────────────────────────────────────────────

        functions.put("digits", args -> {
            requireArgs("digits", args, 1);
            return PuzzMath.digits(toLong(args[0]));
        });

        functions.put("digit_sum", args -> {
            requireArgs("digit_sum", args, 1);
            return PuzzMath.digitSum(toLong(args[0]));
        });

        functions.put("digit_count", args -> {
            requireArgs("digit_count", args, 1);
            return PuzzMath.digitCount(toLong(args[0]));
        });

        functions.put("from_digits", args -> {
            requireArgs("from_digits", args, 1);
            if (!(args[0] instanceof PuzzList list)) {
                throw new PuzzLangException("from_digits requires a list argument");
            }
            long result = 0;
            for (int i = 0; i < list.size(); i++) {
                result = result * 10 + toLong(list.get(i));
            }
            return result;
        });

        functions.put("reverse_digits", args -> {
            requireArgs("reverse_digits", args, 1);
            return PuzzMath.reverseDigits(toLong(args[0]));
        });

        functions.put("is_palindrome", args -> {
            requireArgs("is_palindrome", args, 1);
            return PuzzMath.isPalindrome(toLong(args[0]));
        });

        // ── Combinatorics ───────────────────────────────────────────────────

        functions.put("factorial", args -> {
            requireArgs("factorial", args, 1);
            return PuzzMath.factorial(toInt(args[0]));
        });

        functions.put("binomial", args -> {
            requireArgs("binomial", args, 2);
            return PuzzMath.binomial(toInt(args[0]), toInt(args[1]));
        });

        // ── Modular arithmetic ──────────────────────────────────────────────

        functions.put("mod", args -> {
            requireArgs("mod", args, 2);
            return PuzzMath.mod(toLong(args[0]), toLong(args[1]));
        });

        functions.put("mod_pow", args -> {
            requireArgs("mod_pow", args, 3);
            return PuzzMath.modPow(toLong(args[0]), toLong(args[1]), toLong(args[2]));
        });

        functions.put("mod_inverse", args -> {
            requireArgs("mod_inverse", args, 2);
            return PuzzMath.modInverse(toLong(args[0]), toLong(args[1]));
        });

        // ── Bit operations ──────────────────────────────────────────────────

        functions.put("popcount", args -> {
            requireArgs("popcount", args, 1);
            return PuzzMath.popcount(toLong(args[0]));
        });

        functions.put("bit_count", args -> {
            requireArgs("bit_count", args, 1);
            return PuzzMath.popcount(toLong(args[0]));
        });

        functions.put("highest_bit", args -> {
            requireArgs("highest_bit", args, 1);
            return PuzzMath.highestBit(toLong(args[0]));
        });

        functions.put("lowest_bit", args -> {
            requireArgs("lowest_bit", args, 1);
            return PuzzMath.lowestBit(toLong(args[0]));
        });

        // ── Predicates ──────────────────────────────────────────────────────

        functions.put("is_even", args -> {
            requireArgs("is_even", args, 1);
            return toLong(args[0]) % 2 == 0;
        });

        functions.put("is_odd", args -> {
            requireArgs("is_odd", args, 1);
            return toLong(args[0]) % 2 != 0;
        });

        functions.put("is_perfect_square", args -> {
            requireArgs("is_perfect_square", args, 1);
            return PuzzMath.isPerfectSquare(toLong(args[0]));
        });

        // ── Division ────────────────────────────────────────────────────────

        functions.put("divmod", args -> {
            requireArgs("divmod", args, 2);
            return PuzzMath.divmod(toLong(args[0]), toLong(args[1]));
        });

        functions.put("floor_div", args -> {
            requireArgs("floor_div", args, 2);
            return PuzzMath.floorDiv(toLong(args[0]), toLong(args[1]));
        });

        functions.put("ceil_div", args -> {
            requireArgs("ceil_div", args, 2);
            return PuzzMath.ceilDiv(toLong(args[0]), toLong(args[1]));
        });

        // ── Base conversion ─────────────────────────────────────────────────

        functions.put("to_base", args -> {
            requireArgs("to_base", args, 2);
            int base = toInt(args[1]);
            if (base < 2 || base > 36) {
                throw new PuzzLangException("to_base requires base between 2 and 36, got %d", base);
            }
            return Long.toString(toLong(args[0]), base);
        });

        functions.put("from_base", args -> {
            requireArgs("from_base", args, 2);
            String str = args[0].toString();
            int base = toInt(args[1]);
            if (base < 2 || base > 36) {
                throw new PuzzLangException("from_base requires base between 2 and 36, got %d", base);
            }
            return Long.parseLong(str, base);
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════════════════

    private static void requireArgs(String name, Object[] args, int expected) {
        if (args.length != expected) {
            throw PuzzLangException.argumentCount(name, expected, args.length);
        }
    }

    private static void requireMinArgs(String name, Object[] args, int min) {
        if (args.length < min) {
            throw new PuzzLangException("%s requires at least %d argument(s), got %d", 
                    name, min, args.length);
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
