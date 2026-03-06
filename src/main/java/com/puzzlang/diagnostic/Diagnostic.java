package com.puzzlang.diagnostic;

import com.puzzlang.ast.SourceLocation;

/**
 * Represents a compiler diagnostic (error or warning).
 * 
 * Each diagnostic has:
 * - A source location (where in the code)
 * - An error code (for programmatic handling, e.g., "E001")
 * - A human-readable message
 */
public sealed interface Diagnostic permits Diagnostic.Error, Diagnostic.Warning, Diagnostic.Info {
    
    SourceLocation location();
    String code();
    String message();
    Severity severity();
    
    enum Severity { ERROR, WARNING, INFO }
    
    /**
     * Compilation error — prevents successful compilation.
     */
    record Error(SourceLocation location, String code, String message) implements Diagnostic {
        @Override public Severity severity() { return Severity.ERROR; }
    }
    
    /**
     * Warning — compilation proceeds but something looks suspicious.
     */
    record Warning(SourceLocation location, String code, String message) implements Diagnostic {
        @Override public Severity severity() { return Severity.WARNING; }
    }
    
    /**
     * Informational note — hints for better code.
     */
    record Info(SourceLocation location, String code, String message) implements Diagnostic {
        @Override public Severity severity() { return Severity.INFO; }
    }
    
    /**
     * Formats the diagnostic for display.
     */
    default String format() {
        String prefix = switch (severity()) {
            case ERROR -> "error";
            case WARNING -> "warning";
            case INFO -> "info";
        };
        
        if (location().equals(SourceLocation.UNKNOWN)) {
            return "%s[%s]: %s".formatted(prefix, code(), message());
        }
        return "%s:%d:%d: %s[%s]: %s".formatted(
            location().file(),
            location().line(),
            location().column(),
            prefix,
            code(),
            message()
        );
    }
}
