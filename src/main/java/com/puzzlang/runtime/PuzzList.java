package com.puzzlang.runtime;

import java.util.*;

/**
 * PuzzList — a first-class list value for PuzzLang.
 *
 * Wraps a Java List and provides AoC-friendly operations.
 * Implements Iterable for for-in loop support.
 *
 * SUPPORTED OPERATIONS (callable via .method() syntax):
 *
 *   Iteration:
 *     for x in myList: ...
 *
 *   Access:
 *     list.count()              → number of elements
 *     list.length()             → alias for count
 *     list.size()               → alias for count
 *     list.first()              → first element (error if empty)
 *     list.last()               → last element (error if empty)
 *     list.get(index)           → element at index (0-based)
 *     list.isEmpty()            → true if no elements
 *
 *   Transformation:
 *     list.join(sep)            → concatenate with separator
 *     list.reversed()           → new list in reverse order
 *     list.sorted()             → new sorted list
 *     list.unique()             → new list with duplicates removed
 *
 *   Aggregation:
 *     list.sum()                → sum of numeric elements
 *     list.product()            → product of numeric elements
 *     list.min()                → minimum value
 *     list.max()                → maximum value
 *
 *   Search:
 *     list.contains(val)        → true if val is in list
 *     list.indexOf(val)         → index of first occurrence, -1 if not found
 */
public class PuzzList implements Iterable<Object> {

    private final List<Object> elements;

    public PuzzList() {
        this.elements = new ArrayList<>();
    }

    public PuzzList(List<?> items) {
        this.elements = new ArrayList<>(items);
    }

    // ── Access ───────────────────────────────────────────────────────────────

    public int size() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public Object first() {
        if (elements.isEmpty()) {
            throw new PuzzLangException("first() on empty list");
        }
        return elements.get(0);
    }

    public Object last() {
        if (elements.isEmpty()) {
            throw new PuzzLangException("last() on empty list");
        }
        return elements.get(elements.size() - 1);
    }

    public Object get(int index) {
        if (index < 0 || index >= elements.size()) {
            throw new PuzzLangException("list index %d out of bounds (size=%d)", 
                    index, elements.size());
        }
        return elements.get(index);
    }

    // ── Mutation (for building lists) ────────────────────────────────────────

    public void add(Object item) {
        elements.add(item);
    }

    public void addAll(PuzzList other) {
        elements.addAll(other.elements);
    }

    // ── Transformation ───────────────────────────────────────────────────────

    public String join(String separator) {
        StringJoiner joiner = new StringJoiner(separator);
        for (Object elem : elements) {
            joiner.add(String.valueOf(elem));
        }
        return joiner.toString();
    }

    public PuzzList reversed() {
        List<Object> rev = new ArrayList<>(elements);
        Collections.reverse(rev);
        return new PuzzList(rev);
    }

    /**
     * Returns a new sorted list.
     * Elements must be comparable (typically numbers or strings).
     */
    @SuppressWarnings("unchecked")
    public PuzzList sorted() {
        List<Object> copy = new ArrayList<>(elements);
        copy.sort((a, b) -> {
            // Handle numeric comparison
            if (a instanceof Number && b instanceof Number) {
                double da = ((Number) a).doubleValue();
                double db = ((Number) b).doubleValue();
                return Double.compare(da, db);
            }
            // Handle comparable types
            if (a instanceof Comparable && b instanceof Comparable) {
                try {
                    return ((Comparable<Object>) a).compareTo(b);
                } catch (ClassCastException e) {
                    // Fall through to string comparison
                }
            }
            // Fallback: compare as strings
            return String.valueOf(a).compareTo(String.valueOf(b));
        });
        return new PuzzList(copy);
    }

    /**
     * Returns a new list with duplicates removed (preserves first occurrence order).
     */
    public PuzzList unique() {
        Set<Object> seen = new LinkedHashSet<>();
        for (Object elem : elements) {
            seen.add(elem);
        }
        return new PuzzList(new ArrayList<>(seen));
    }

    /**
     * Returns a new list with elements in the specified range.
     */
    public PuzzList slice(int start, int end) {
        int actualStart = Math.max(0, start);
        int actualEnd = Math.min(elements.size(), end);
        if (actualStart >= actualEnd) {
            return new PuzzList();
        }
        return new PuzzList(elements.subList(actualStart, actualEnd));
    }

    /**
     * Returns a new list with elements from start to end.
     */
    public PuzzList slice(int start) {
        return slice(start, elements.size());
    }

    /**
     * Count occurrences of a value in the list.
     */
    public int countOccurrences(Object value) {
        int count = 0;
        for (Object elem : elements) {
            if (Objects.equals(elem, value)) {
                count++;
            }
        }
        return count;
    }

    // ── Search ───────────────────────────────────────────────────────────────

    public boolean contains(Object value) {
        return elements.contains(value);
    }

    public int indexOf(Object value) {
        return elements.indexOf(value);
    }

    public int lastIndexOf(Object value) {
        return elements.lastIndexOf(value);
    }

    /**
     * Count occurrences of a value.
     */
    public int count(Object value) {
        int count = 0;
        for (Object elem : elements) {
            if (Objects.equals(elem, value)) {
                count++;
            }
        }
        return count;
    }

    // ── Aggregation ──────────────────────────────────────────────────────────

    /**
     * Sum of all numeric elements.
     */
    public long sum() {
        long sum = 0;
        for (Object elem : elements) {
            sum += toLong(elem);
        }
        return sum;
    }

    /**
     * Product of all numeric elements.
     */
    public long product() {
        if (elements.isEmpty()) return 1;
        long product = 1;
        for (Object elem : elements) {
            product *= toLong(elem);
        }
        return product;
    }

    /**
     * Minimum value (numeric comparison).
     */
    public long min() {
        if (elements.isEmpty()) {
            throw new PuzzLangException("min() on empty list");
        }
        long min = toLong(elements.get(0));
        for (int i = 1; i < elements.size(); i++) {
            min = Math.min(min, toLong(elements.get(i)));
        }
        return min;
    }

    /**
     * Maximum value (numeric comparison).
     */
    public long max() {
        if (elements.isEmpty()) {
            throw new PuzzLangException("max() on empty list");
        }
        long max = toLong(elements.get(0));
        for (int i = 1; i < elements.size(); i++) {
            max = Math.max(max, toLong(elements.get(i)));
        }
        return max;
    }

    // ── Iteration ────────────────────────────────────────────────────────────

    @Override
    public Iterator<Object> iterator() {
        return elements.iterator();
    }

    // ── Display ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        for (Object elem : elements) {
            if (elem instanceof String) {
                sj.add("\"" + elem + "\"");
            } else {
                sj.add(String.valueOf(elem));
            }
        }
        return sj.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PuzzList other)) return false;
        return elements.equals(other.elements);
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    /**
     * Returns the underlying list (for internal use).
     */
    public List<Object> toJavaList() {
        return new ArrayList<>(elements);
    }

    private static long toLong(Object obj) {
        if (obj instanceof Long l) return l;
        if (obj instanceof Integer i) return i.longValue();
        if (obj instanceof Number n) return n.longValue();
        throw new PuzzLangException("expected number, got %s", obj.getClass().getSimpleName());
    }
}
