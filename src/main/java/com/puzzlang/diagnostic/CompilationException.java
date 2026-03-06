package com.puzzlang.diagnostic;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Exception thrown when compilation fails due to one or more errors.
 * 
 * Contains all diagnostics collected during compilation, allowing
 * callers to inspect and format errors as needed.
 */
public class CompilationException extends RuntimeException {
    
    private final List<Diagnostic> diagnostics;
    
    public CompilationException(List<Diagnostic> diagnostics) {
        super(formatMessage(diagnostics));
        this.diagnostics = List.copyOf(diagnostics);
    }
    
    public CompilationException(String message) {
        super(message);
        this.diagnostics = List.of();
    }
    
    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }
    
    public List<Diagnostic> getErrors() {
        return diagnostics.stream()
            .filter(d -> d.severity() == Diagnostic.Severity.ERROR)
            .collect(Collectors.toList());
    }
    
    public int errorCount() {
        return (int) diagnostics.stream()
            .filter(d -> d.severity() == Diagnostic.Severity.ERROR)
            .count();
    }
    
    private static String formatMessage(List<Diagnostic> diagnostics) {
        if (diagnostics.isEmpty()) {
            return "Compilation failed";
        }
        
        long errorCount = diagnostics.stream()
            .filter(d -> d.severity() == Diagnostic.Severity.ERROR)
            .count();
        
        StringBuilder sb = new StringBuilder();
        sb.append("Compilation failed with ").append(errorCount).append(" error(s):\n");
        
        diagnostics.stream()
            .filter(d -> d.severity() == Diagnostic.Severity.ERROR)
            .limit(5)  // Show first 5 errors
            .forEach(d -> sb.append("  ").append(d.format()).append("\n"));
        
        if (errorCount > 5) {
            sb.append("  ... and ").append(errorCount - 5).append(" more error(s)\n");
        }
        
        return sb.toString().stripTrailing();
    }
}
