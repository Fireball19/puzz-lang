package com.puzzlang.runtime;

/**
 * Runtime exception for PuzzLang execution errors.
 * 
 * Provides formatted error messages with context about what went wrong.
 */
public class PuzzLangException extends RuntimeException {
    
    public PuzzLangException(String message) {
        super("PuzzLang: " + message);
    }
    
    public PuzzLangException(String format, Object... args) {
        super("PuzzLang: " + format.formatted(args));
    }
    
    public PuzzLangException(String message, Throwable cause) {
        super("PuzzLang: " + message, cause);
    }
    
    // ── Factory methods for common errors ───────────────────────────────────
    
    public static PuzzLangException undefinedMethod(String method, Object receiver) {
        String typeName = receiver == null ? "null" : receiver.getClass().getSimpleName();
        return new PuzzLangException("no method '%s' on %s", method, typeName);
    }
    
    public static PuzzLangException typeMismatch(String expected, Object actual) {
        String actualType = actual == null ? "null" : actual.getClass().getSimpleName();
        return new PuzzLangException("expected %s, got %s", expected, actualType);
    }
    
    public static PuzzLangException divisionByZero() {
        return new PuzzLangException("division by zero");
    }
    
    public static PuzzLangException notIterable(Object value) {
        String typeName = value == null ? "null" : value.getClass().getSimpleName();
        return new PuzzLangException("'%s' is not iterable", typeName);
    }
    
    public static PuzzLangException argumentCount(String method, int expected, int actual) {
        return new PuzzLangException("method '%s' expects %d argument(s), got %d", 
            method, expected, actual);
    }
    
    public static PuzzLangException emptyRange(String operation) {
        return new PuzzLangException("%s on empty range", operation);
    }
    
    public static PuzzLangException conversionFailed(String value, String targetType) {
        return new PuzzLangException("cannot convert '%s' to %s", value, targetType);
    }
}
