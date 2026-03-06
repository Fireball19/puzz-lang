package com.puzzlang.runtime;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * PuzzRange — a first-class range/interval value.
 *
 * Represents an integer range [start, end) or [start, end].
 * Ranges are lazy — they do not allocate an array of elements.
 *
 * SUPPORTED OPERATIONS (callable from PuzzLang via .method() syntax):
 *
 *   Iteration:
 *     for x in 1..10: ...        — iterates start..end-1
 *
 *   Numeric aggregation:
 *     (1..=100).sum()            → 5050
 *     (1..=10).product()         → 3628800
 *     (1..10).count()            → 9  (number of elements)
 *     (1..=5).min()              → 1
 *     (1..=5).max()              → 5
 *
 *   Membership:
 *     (1..10).contains(5)        → true
 *     (1..10).contains(10)       → false  (exclusive end)
 *     (1..=10).contains(10)      → true   (inclusive end)
 *
 *   Overlap / intersection:
 *     (1..5).overlaps(3..8)      → true
 *     (1..5).overlaps(5..8)      → false  (exclusive: 5 not in 1..5)
 *
 *   Conversion:
 *     (1..5).toList()            → List [1,2,3,4]
 *
 *   Inspection:
 *     r.start()                  → start value
 *     r.end()                    → end value (exclusive or inclusive)
 *     r.isInclusive()            → true if ..= range
 *     r.isEmpty()                → true if no elements
 *
 *   Step / transform (returns new range or list):
 *     (0..10).step(2)            → every other element: 0,2,4,6,8
 */
public class PuzzRange implements Iterable<Integer> {

    private final int start;
    private final int end;           // always the raw 'end' value from the source
    private final boolean inclusive; // true → ..=  false → ..

    public PuzzRange(int start, int end, boolean inclusive) {
        this.start     = start;
        this.end       = end;
        this.inclusive = inclusive;
    }

    // ── Computed boundary ────────────────────────────────────────────────────

    /** The first integer in the range. */
    public int startValue() { return start; }

    /**
     * The last integer that is actually part of the range.
     *   1..5   → lastValue() == 4
     *   1..=5  → lastValue() == 5
     */
    public int lastValue() { return inclusive ? end : end - 1; }

    /** Total number of elements in the range (0 if empty). */
    public int count() { return Math.max(0, lastValue() - start + 1); }

    public boolean inclusive() { return inclusive; }

    public boolean isEmpty() { return count() == 0; }

    // ── Membership ───────────────────────────────────────────────────────────

    public boolean contains(int value) {
        return value >= start && (inclusive ? value <= end : value < end);
    }

    // ── Overlap / intersection ───────────────────────────────────────────────

    /**
     * Two ranges overlap if they share at least one integer element.
     *
     * Note: this compares the *effective* element sets, not the raw bounds.
     * So (1..5).overlaps(5..8) is false because 5 ∉ {1,2,3,4}.
     */
    public boolean overlaps(PuzzRange other) {
        if (this.isEmpty() || other.isEmpty()) return false;
        return this.lastValue()  >= other.startValue()
                && other.lastValue() >= this.startValue();
    }

    /**
     * Returns a new range representing the intersection, or null if no overlap.
     */
    public PuzzRange intersection(PuzzRange other) {
        if (!overlaps(other)) return null;
        int newStart = Math.max(this.startValue(), other.startValue());
        int newEnd   = Math.min(this.lastValue(),  other.lastValue());
        return new PuzzRange(newStart, newEnd, true);  // always inclusive result
    }

    // ── Aggregation ──────────────────────────────────────────────────────────

    public long sum() {
        // Use Gauss formula for O(1) performance — no iteration needed
        long n = count();
        if (n == 0) return 0;
        // Sum of arithmetic series: n * (first + last) / 2
        return n * (start + lastValue()) / 2;
    }

    public long product() {
        long result = 1;
        for (int i = start; i <= lastValue(); i++) result *= i;
        return result;
    }

    public int min() {
        if (isEmpty()) throw PuzzLangException.emptyRange("min()");
        return start;  // ranges are always ascending (for now)
    }

    public int max() {
        if (isEmpty()) throw PuzzLangException.emptyRange("max()");
        return lastValue();
    }

    // ── Step ─────────────────────────────────────────────────────────────────

    /**
     * Returns a SteppedRange for iteration with custom step size.
     */
    public SteppedRange step(int stepSize) {
        if (stepSize <= 0) {
            throw new PuzzLangException("step must be > 0, got %d", stepSize);
        }
        return new SteppedRange(start, lastValue(), stepSize);
    }

    // ── Iterable / Iterator ──────────────────────────────────────────────────

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<>() {
            private int current = start;
            private final int limit = lastValue();

            @Override public boolean hasNext() { return current <= limit; }
            @Override public Integer next() {
                if (!hasNext()) throw new NoSuchElementException();
                return current++;
            }
        };
    }

    // ── Display ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return inclusive
                ? start + "..=" + end
                : start + ".."  + end;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PuzzRange r) {
            return startValue() == r.startValue() && lastValue() == r.lastValue();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * startValue() + lastValue();
    }

    // ── Inner: SteppedRange ──────────────────────────────────────────────────

    /**
     * A range iterated with a custom step size.
     * Produced by range.step(n).
     */
    public static class SteppedRange implements Iterable<Integer> {
        private final int start, end, step;

        public SteppedRange(int start, int end, int step) {
            this.start = start;
            this.end   = end;
            this.step  = step;
        }

        public long sum() {
            long total = 0;
            for (int i = start; i <= end; i += step) total += i;
            return total;
        }

        public int count() {
            if (end < start) return 0;
            return (end - start) / step + 1;
        }

        @Override
        public Iterator<Integer> iterator() {
            return new Iterator<>() {
                int current = start;
                @Override public boolean hasNext() { return current <= end; }
                @Override public Integer next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    int v = current;
                    current += step;
                    return v;
                }
            };
        }

        @Override
        public String toString() {
            return start + ".." + (end + 1) + " step " + step;
        }
    }
}
