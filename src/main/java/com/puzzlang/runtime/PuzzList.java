package com.puzzlang.runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

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
 *     list.first()              → first element (error if empty)
 *     list.last()               → last element (error if empty)
 *     list.get(index)           → element at index (0-based)
 *     list.isEmpty()            → true if no elements
 *
 *   Transformation:
 *     list.join(sep)            → concatenate with separator
 *     list.reversed()           → new list in reverse order
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
            throw new RuntimeException("PuzzLang: first() on empty list");
        }
        return elements.get(0);
    }

    public Object last() {
        if (elements.isEmpty()) {
            throw new RuntimeException("PuzzLang: last() on empty list");
        }
        return elements.get(elements.size() - 1);
    }

    public Object get(int index) {
        if (index < 0 || index >= elements.size()) {
            throw new RuntimeException("PuzzLang: list index " + index + 
                " out of bounds (size=" + elements.size() + ")");
        }
        return elements.get(index);
    }

    // ── Mutation (for building lists) ────────────────────────────────────────

    public void add(Object item) {
        elements.add(item);
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
        java.util.Collections.reverse(rev);
        return new PuzzList(rev);
    }

    // ── Search ───────────────────────────────────────────────────────────────

    public boolean contains(Object value) {
        return elements.contains(value);
    }

    public int indexOf(Object value) {
        return elements.indexOf(value);
    }

    // ── Iterable ─────────────────────────────────────────────────────────────

    @Override
    public Iterator<Object> iterator() {
        return elements.iterator();
    }

    // ── Display ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        for (Object elem : elements) {
            if (elem instanceof String s) {
                sj.add("\"" + s + "\"");
            } else {
                sj.add(String.valueOf(elem));
            }
        }
        return sj.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PuzzList other) {
            return elements.equals(other.elements);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    // ── Access to underlying list (for runtime) ──────────────────────────────

    public List<Object> getElements() {
        return elements;
    }
}
