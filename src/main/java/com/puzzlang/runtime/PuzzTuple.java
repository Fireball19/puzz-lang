package com.puzzlang.runtime;

import java.util.*;

/**
 * PuzzTuple — a first-class immutable tuple value for PuzzLang.
 *
 * Tuples are fixed-size, heterogeneous containers that support:
 *   - Indexed access: tuple.get(0), tuple.get(1)
 *   - Destructuring: let (x, y) = tuple
 *   - Pattern matching: match point: (0, 0) -> ...
 *   - Equality: (1, 2) == (1, 2)
 *   - Iteration: for x in tuple
 *
 * Unlike lists, tuples:
 *   - Have fixed size determined at creation
 *   - Are immutable (no add/remove)
 *   - Are commonly used for multiple return values and coordinates
 *
 * SUPPORTED OPERATIONS:
 *
 *   Access:
 *     tuple.get(index)           → element at index (0-based)
 *     tuple.first()              → first element
 *     tuple.last()               → last element
 *     tuple.size()               → number of elements
 *     tuple.count()              → alias for size
 *
 *   Conversion:
 *     tuple.toList()             → convert to PuzzList
 *
 *   Unpacking (used internally for destructuring):
 *     tuple.unpack(n)            → verify size == n, return elements
 */
public class PuzzTuple implements Iterable<Object> {

    private final List<Object> elements;

    public PuzzTuple(Object... values) {
        this.elements = List.of(values);  // Immutable list
    }

    public PuzzTuple(List<?> values) {
        this.elements = List.copyOf(values);  // Immutable copy
    }

    // ── Factory Methods ──────────────────────────────────────────────────────

    /**
     * Create a tuple from varargs.
     */
    public static PuzzTuple of(Object... values) {
        return new PuzzTuple(values);
    }

    /**
     * Create a pair (2-tuple).
     */
    public static PuzzTuple pair(Object a, Object b) {
        return new PuzzTuple(a, b);
    }

    /**
     * Create a triple (3-tuple).
     */
    public static PuzzTuple triple(Object a, Object b, Object c) {
        return new PuzzTuple(a, b, c);
    }

    // ── Access ───────────────────────────────────────────────────────────────

    public int size() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public Object get(int index) {
        if (index < 0 || index >= elements.size()) {
            throw new PuzzLangException("tuple index %d out of bounds (size=%d)",
                    index, elements.size());
        }
        return elements.get(index);
    }

    public Object first() {
        if (elements.isEmpty()) {
            throw new PuzzLangException("first() on empty tuple");
        }
        return elements.get(0);
    }

    public Object second() {
        if (elements.size() < 2) {
            throw new PuzzLangException("second() requires tuple of size >= 2, got size=%d", elements.size());
        }
        return elements.get(1);
    }

    public Object third() {
        if (elements.size() < 3) {
            throw new PuzzLangException("third() requires tuple of size >= 3, got size=%d", elements.size());
        }
        return elements.get(2);
    }

    public Object last() {
        if (elements.isEmpty()) {
            throw new PuzzLangException("last() on empty tuple");
        }
        return elements.get(elements.size() - 1);
    }

    // ── Unpacking (for destructuring) ────────────────────────────────────────

    /**
     * Verify this tuple has exactly n elements and return the elements.
     * Used by bytecode emitter for destructuring.
     *
     * @param n Expected number of elements
     * @return Array of elements
     * @throws PuzzLangException if size doesn't match
     */
    public Object[] unpack(int n) {
        if (elements.size() != n) {
            throw new PuzzLangException(
                    "cannot unpack tuple of size %d into %d variables",
                    elements.size(), n);
        }
        return elements.toArray();
    }

    /**
     * Get all elements as an array (without size checking).
     */
    public Object[] toArray() {
        return elements.toArray();
    }

    // ── Conversion ───────────────────────────────────────────────────────────

    /**
     * Convert to a PuzzList.
     */
    public PuzzList toList() {
        return new PuzzList(elements);
    }

    // ── Iteration ────────────────────────────────────────────────────────────

    @Override
    public Iterator<Object> iterator() {
        return elements.iterator();
    }

    // ── Equality and Hashing ─────────────────────────────────────────────────

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PuzzTuple other)) return false;
        return elements.equals(other.elements);
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    // ── Display ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ", "(", ")");
        for (Object elem : elements) {
            sj.add(formatElement(elem));
        }
        return sj.toString();
    }

    private String formatElement(Object elem) {
        if (elem instanceof String s) {
            return "\"" + s + "\"";
        }
        return String.valueOf(elem);
    }

    // ── Internal access ──────────────────────────────────────────────────────

    List<Object> getElements() {
        return elements;
    }
}
