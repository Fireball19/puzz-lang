package com.puzzlang.diagnostic;

import com.puzzlang.ast.SourceLocation;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Collects diagnostics (errors, warnings, info) during compilation.
 * 
 * Usage:
 *   DiagnosticCollector diag = new DiagnosticCollector();
 *   diag.error(loc, "E001", "Undefined variable '%s'", varName);
 *   diag.warning(loc, "W001", "Unused variable '%s'", varName);
 *   
 *   if (diag.hasErrors()) {
 *       diag.printAll(System.err);
 *       throw diag.toException();
 *   }
 */
public class DiagnosticCollector {
    
    private final List<Diagnostic> diagnostics = new ArrayList<>();
    private final String sourceName;
    
    public DiagnosticCollector() {
        this("<input>");
    }
    
    public DiagnosticCollector(String sourceName) {
        this.sourceName = sourceName;
    }
    
    // ── Recording diagnostics ───────────────────────────────────────────────
    
    public void error(SourceLocation location, String code, String message, Object... args) {
        diagnostics.add(new Diagnostic.Error(
            location.withFile(sourceName),
            code,
            message.formatted(args)
        ));
    }
    
    public void warning(SourceLocation location, String code, String message, Object... args) {
        diagnostics.add(new Diagnostic.Warning(
            location.withFile(sourceName),
            code,
            message.formatted(args)
        ));
    }
    
    public void info(SourceLocation location, String code, String message, Object... args) {
        diagnostics.add(new Diagnostic.Info(
            location.withFile(sourceName),
            code,
            message.formatted(args)
        ));
    }
    
    public void add(Diagnostic diagnostic) {
        diagnostics.add(diagnostic);
    }
    
    // ── Querying ────────────────────────────────────────────────────────────
    
    public boolean hasErrors() {
        return diagnostics.stream().anyMatch(d -> d.severity() == Diagnostic.Severity.ERROR);
    }
    
    public boolean hasWarnings() {
        return diagnostics.stream().anyMatch(d -> d.severity() == Diagnostic.Severity.WARNING);
    }
    
    public boolean isEmpty() {
        return diagnostics.isEmpty();
    }
    
    public int errorCount() {
        return (int) diagnostics.stream()
            .filter(d -> d.severity() == Diagnostic.Severity.ERROR)
            .count();
    }
    
    public int warningCount() {
        return (int) diagnostics.stream()
            .filter(d -> d.severity() == Diagnostic.Severity.WARNING)
            .count();
    }
    
    public List<Diagnostic> getAll() {
        return Collections.unmodifiableList(diagnostics);
    }
    
    public List<Diagnostic> getErrors() {
        return diagnostics.stream()
            .filter(d -> d.severity() == Diagnostic.Severity.ERROR)
            .collect(Collectors.toList());
    }
    
    public List<Diagnostic> getWarnings() {
        return diagnostics.stream()
            .filter(d -> d.severity() == Diagnostic.Severity.WARNING)
            .collect(Collectors.toList());
    }
    
    // ── Output ──────────────────────────────────────────────────────────────
    
    public void printAll(PrintStream out) {
        for (Diagnostic d : diagnostics) {
            out.println(d.format());
        }
        
        if (hasErrors() || hasWarnings()) {
            out.printf("%n%d error(s), %d warning(s)%n", errorCount(), warningCount());
        }
    }
    
    /**
     * Throws CompilationException if there are any errors.
     */
    public void throwIfErrors() {
        if (hasErrors()) {
            throw toException();
        }
    }
    
    public CompilationException toException() {
        return new CompilationException(new ArrayList<>(diagnostics));
    }
    
    /**
     * Clears all collected diagnostics.
     */
    public void clear() {
        diagnostics.clear();
    }
}
