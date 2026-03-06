package com.puzzlang.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PuzzMath — Core mathematical utilities for PuzzLang.
 *
 * Provides efficient implementations of common math operations
 * needed for puzzle solving (e.g., Advent of Code).
 *
 * Features:
 *   - GCD/LCM using Euclidean algorithm
 *   - Prime generation via Sieve of Eratosthenes
 *   - Primality testing (trial division + Miller-Rabin for large numbers)
 *   - Integer factorization
 *   - Digit manipulation
 *   - Modular arithmetic
 */
public class PuzzMath {

    private PuzzMath() {} // Static utility class

    // ══════════════════════════════════════════════════════════════════════════
    //  GCD / LCM
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Greatest Common Divisor using Euclidean algorithm.
     * Works with negative numbers (returns positive result).
     */
    public static long gcd(long a, long b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            long t = b;
            b = a % b;
            a = t;
        }
        return a;
    }

    /**
     * Least Common Multiple.
     * Uses GCD to avoid overflow: lcm(a,b) = |a| * (|b| / gcd(a,b))
     */
    public static long lcm(long a, long b) {
        if (a == 0 || b == 0) return 0;
        a = Math.abs(a);
        b = Math.abs(b);
        return a / gcd(a, b) * b;  // Divide first to reduce overflow risk
    }

    /**
     * GCD of multiple numbers.
     */
    public static long gcd(long... nums) {
        if (nums.length == 0) return 0;
        long result = nums[0];
        for (int i = 1; i < nums.length; i++) {
            result = gcd(result, nums[i]);
            if (result == 1) return 1;  // Can't get smaller
        }
        return result;
    }

    /**
     * LCM of multiple numbers.
     */
    public static long lcm(long... nums) {
        if (nums.length == 0) return 0;
        long result = nums[0];
        for (int i = 1; i < nums.length; i++) {
            result = lcm(result, nums[i]);
            if (result == 0) return 0;
        }
        return result;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Primality Testing
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Checks if n is prime.
     * Uses trial division for small numbers, Miller-Rabin for large numbers.
     */
    public static boolean isPrime(long n) {
        if (n < 2) return false;
        if (n == 2 || n == 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        
        // For small numbers, use trial division
        if (n < 1_000_000) {
            return isPrimeTrialDivision(n);
        }
        
        // For larger numbers, use Miller-Rabin
        return isPrimeMillerRabin(n);
    }

    private static boolean isPrimeTrialDivision(long n) {
        // Check divisibility by 6k ± 1 up to sqrt(n)
        long limit = (long) Math.sqrt(n) + 1;
        for (long i = 5; i <= limit; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Miller-Rabin primality test.
     * Deterministic for n < 3,317,044,064,679,887,385,961,981
     */
    private static boolean isPrimeMillerRabin(long n) {
        // Write n-1 as 2^r * d
        long d = n - 1;
        int r = 0;
        while ((d & 1) == 0) {
            d >>= 1;
            r++;
        }

        // Witnesses that work for all n < 3,317,044,064,679,887,385,961,981
        long[] witnesses = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37};
        
        for (long a : witnesses) {
            if (a >= n) continue;
            if (!millerRabinWitness(a, d, n, r)) {
                return false;
            }
        }
        return true;
    }

    private static boolean millerRabinWitness(long a, long d, long n, int r) {
        long x = modPow(a, d, n);
        
        if (x == 1 || x == n - 1) return true;
        
        for (int i = 0; i < r - 1; i++) {
            x = modMul(x, x, n);
            if (x == n - 1) return true;
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Prime Generation (Sieve of Eratosthenes)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Returns all primes up to and including n.
     * Uses Sieve of Eratosthenes - O(n log log n) time, O(n) space.
     */
    public static PuzzList primesUpTo(int n) {
        if (n < 2) return new PuzzList();
        
        boolean[] isComposite = new boolean[n + 1];
        
        // Mark composites
        for (int i = 2; i * i <= n; i++) {
            if (!isComposite[i]) {
                for (int j = i * i; j <= n; j += i) {
                    isComposite[j] = true;
                }
            }
        }
        
        // Collect primes
        PuzzList primes = new PuzzList();
        for (int i = 2; i <= n; i++) {
            if (!isComposite[i]) {
                primes.add(i);
            }
        }
        return primes;
    }

    /**
     * Returns the n-th prime (1-indexed: nthPrime(1) = 2).
     */
    public static long nthPrime(int n) {
        if (n < 1) throw new PuzzLangException("nthPrime requires n >= 1, got %d", n);
        if (n == 1) return 2;
        if (n == 2) return 3;
        
        // Estimate upper bound for n-th prime using prime number theorem
        // p_n < n * (ln(n) + ln(ln(n))) for n >= 6
        double logN = Math.log(n);
        int upperBound = (int) (n * (logN + Math.log(logN) + 2));
        upperBound = Math.max(upperBound, 100);
        
        PuzzList primes = primesUpTo(upperBound);
        if (primes.size() >= n) {
            return ((Number) primes.get(n - 1)).longValue();
        }
        
        // If estimate was too low, search sequentially
        long candidate = upperBound + 1;
        int count = primes.size();
        while (count < n) {
            if (isPrime(candidate)) count++;
            if (count < n) candidate++;
        }
        return candidate;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Factorization
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Returns the prime factorization of n as a list.
     * Example: primeFactors(12) = [2, 2, 3]
     */
    public static PuzzList primeFactors(long n) {
        PuzzList factors = new PuzzList();
        if (n < 2) return factors;
        
        n = Math.abs(n);
        
        // Extract factors of 2
        while (n % 2 == 0) {
            factors.add(2L);
            n /= 2;
        }
        
        // Extract odd factors
        for (long i = 3; i * i <= n; i += 2) {
            while (n % i == 0) {
                factors.add(i);
                n /= i;
            }
        }
        
        // If n is still > 1, it's a prime factor
        if (n > 1) {
            factors.add(n);
        }
        
        return factors;
    }

    /**
     * Returns all divisors of n (including 1 and n).
     * Example: divisors(12) = [1, 2, 3, 4, 6, 12]
     */
    public static PuzzList divisors(long n) {
        PuzzList divs = new PuzzList();
        if (n < 1) return divs;
        
        n = Math.abs(n);
        
        // Find divisors up to sqrt(n)
        List<Long> small = new ArrayList<>();
        List<Long> large = new ArrayList<>();
        
        long sqrt = (long) Math.sqrt(n);
        for (long i = 1; i <= sqrt; i++) {
            if (n % i == 0) {
                small.add(i);
                if (i != n / i) {
                    large.add(n / i);
                }
            }
        }
        
        // Combine in sorted order
        for (Long d : small) divs.add(d);
        for (int i = large.size() - 1; i >= 0; i--) {
            divs.add(large.get(i));
        }
        
        return divs;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Digit Operations
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Returns the digits of n as a list.
     * Example: digits(123) = [1, 2, 3]
     * Note: digits(0) = [0], digits(-123) = [1, 2, 3]
     */
    public static PuzzList digits(long n) {
        n = Math.abs(n);
        
        if (n == 0) {
            PuzzList result = new PuzzList();
            result.add(0);
            return result;
        }
        
        // Count digits first
        List<Integer> digitList = new ArrayList<>();
        while (n > 0) {
            digitList.add((int) (n % 10));
            n /= 10;
        }
        
        // Reverse to get correct order
        PuzzList result = new PuzzList();
        for (int i = digitList.size() - 1; i >= 0; i--) {
            result.add(digitList.get(i));
        }
        return result;
    }

    /**
     * Returns the number of digits in n.
     */
    public static int digitCount(long n) {
        if (n == 0) return 1;
        n = Math.abs(n);
        return (int) Math.log10(n) + 1;
    }

    /**
     * Returns the sum of digits of n.
     */
    public static long digitSum(long n) {
        n = Math.abs(n);
        long sum = 0;
        while (n > 0) {
            sum += n % 10;
            n /= 10;
        }
        return sum;
    }

    /**
     * Reverses the digits of n.
     * Example: reverseDigits(123) = 321
     */
    public static long reverseDigits(long n) {
        boolean negative = n < 0;
        n = Math.abs(n);
        
        long reversed = 0;
        while (n > 0) {
            reversed = reversed * 10 + n % 10;
            n /= 10;
        }
        return negative ? -reversed : reversed;
    }

    /**
     * Checks if n is a palindrome in base 10.
     */
    public static boolean isPalindrome(long n) {
        if (n < 0) return false;
        return n == reverseDigits(n);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Modular Arithmetic
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Proper modulo that always returns non-negative result.
     * Unlike Java's %, this handles negative numbers correctly.
     * Example: mod(-5, 3) = 1, not -2
     */
    public static long mod(long a, long m) {
        if (m <= 0) throw new PuzzLangException("modulus must be positive, got %d", m);
        long result = a % m;
        return result < 0 ? result + m : result;
    }

    /**
     * Modular multiplication that avoids overflow.
     */
    public static long modMul(long a, long b, long m) {
        return Math.floorMod(Math.multiplyExact(Math.floorMod(a, m), Math.floorMod(b, m)), m);
    }

    /**
     * Modular exponentiation: (base^exp) mod m
     * Uses binary exponentiation for O(log exp) time.
     */
    public static long modPow(long base, long exp, long m) {
        if (m == 1) return 0;
        
        long result = 1;
        base = mod(base, m);
        
        while (exp > 0) {
            if ((exp & 1) == 1) {
                result = modMulSafe(result, base, m);
            }
            exp >>= 1;
            base = modMulSafe(base, base, m);
        }
        return result;
    }

    /**
     * Safe modular multiplication using BigInteger for intermediate results
     * to avoid overflow.
     */
    private static long modMulSafe(long a, long b, long m) {
        return java.math.BigInteger.valueOf(a)
                .multiply(java.math.BigInteger.valueOf(b))
                .mod(java.math.BigInteger.valueOf(m))
                .longValue();
    }

    /**
     * Modular inverse using extended Euclidean algorithm.
     * Returns x such that (a * x) % m == 1.
     * Throws if gcd(a, m) != 1.
     */
    public static long modInverse(long a, long m) {
        long[] result = extendedGcd(a, m);
        if (result[0] != 1) {
            throw new PuzzLangException("modular inverse does not exist for %d mod %d", a, m);
        }
        return mod(result[1], m);
    }

    /**
     * Extended Euclidean algorithm.
     * Returns [gcd, x, y] such that a*x + b*y = gcd(a, b).
     */
    public static long[] extendedGcd(long a, long b) {
        if (b == 0) {
            return new long[]{a, 1, 0};
        }
        long[] result = extendedGcd(b, a % b);
        return new long[]{result[0], result[2], result[1] - (a / b) * result[2]};
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Other Utilities
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Integer power function.
     */
    public static long pow(long base, int exp) {
        if (exp < 0) throw new PuzzLangException("negative exponent not supported: %d", exp);
        long result = 1;
        while (exp > 0) {
            if ((exp & 1) == 1) result *= base;
            base *= base;
            exp >>= 1;
        }
        return result;
    }

    /**
     * Integer square root (floor).
     */
    public static long isqrt(long n) {
        if (n < 0) throw new PuzzLangException("isqrt of negative number: %d", n);
        if (n == 0) return 0;
        
        long x = (long) Math.sqrt(n);
        // Newton's method refinement
        while (x * x > n) x--;
        while ((x + 1) * (x + 1) <= n) x++;
        return x;
    }

    /**
     * Checks if n is a perfect square.
     */
    public static boolean isPerfectSquare(long n) {
        if (n < 0) return false;
        long sqrt = isqrt(n);
        return sqrt * sqrt == n;
    }

    /**
     * Absolute value.
     */
    public static long abs(long n) {
        return Math.abs(n);
    }

    /**
     * Sign function: returns -1, 0, or 1.
     */
    public static int sign(long n) {
        return Long.compare(n, 0);
    }

    /**
     * Clamp value to range [min, max].
     */
    public static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Integer division that rounds towards negative infinity (floor division).
     */
    public static long floorDiv(long a, long b) {
        return Math.floorDiv(a, b);
    }

    /**
     * Ceiling division.
     */
    public static long ceilDiv(long a, long b) {
        return -Math.floorDiv(-a, b);
    }

    /**
     * Returns [quotient, remainder] as divmod.
     */
    public static PuzzList divmod(long a, long b) {
        PuzzList result = new PuzzList();
        result.add(a / b);
        result.add(a % b);
        return result;
    }

    /**
     * Binomial coefficient C(n, k) = n! / (k! * (n-k)!)
     */
    public static long binomial(int n, int k) {
        if (k < 0 || k > n) return 0;
        if (k == 0 || k == n) return 1;
        
        // Use symmetry: C(n,k) = C(n, n-k)
        if (k > n - k) k = n - k;
        
        long result = 1;
        for (int i = 0; i < k; i++) {
            result = result * (n - i) / (i + 1);
        }
        return result;
    }

    /**
     * Factorial.
     */
    public static long factorial(int n) {
        if (n < 0) throw new PuzzLangException("factorial of negative number: %d", n);
        if (n > 20) throw new PuzzLangException("factorial(%d) would overflow long", n);
        
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    /**
     * Population count (number of 1 bits).
     */
    public static int popcount(long n) {
        return Long.bitCount(n);
    }

    /**
     * Highest set bit position (0-indexed from right).
     * Returns -1 for n = 0.
     */
    public static int highestBit(long n) {
        if (n == 0) return -1;
        return 63 - Long.numberOfLeadingZeros(n);
    }

    /**
     * Lowest set bit position (0-indexed from right).
     * Returns -1 for n = 0.
     */
    public static int lowestBit(long n) {
        if (n == 0) return -1;
        return Long.numberOfTrailingZeros(n);
    }
}
