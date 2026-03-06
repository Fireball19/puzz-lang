package com.puzzlang.api;

import com.puzzlang.diagnostic.DiagnosticCollector;
import com.puzzlang.runtime.MethodRegistry;
import com.puzzlang.runtime.builtins.RangeBuiltins;

import java.io.PrintStream;

/**
 * Configuration context for the PuzzLang compiler.
 * 
 * Holds all configurable aspects of compilation:
 * - Source identification (for error messages)
 * - Output stream (for print statements)
 * - Diagnostic collector (for errors/warnings)
 * - Method registry (for runtime dispatch)
 * - Compiler flags
 * 
 * Usage:
 *   CompilerContext ctx = CompilerContext.defaults();
 *   // or
 *   CompilerContext ctx = CompilerContext.builder()
 *       .sourceName("myfile.puzz")
 *       .output(myStream)
 *       .build();
 */
public record CompilerContext(
    String sourceName,
    PrintStream output,
    DiagnosticCollector diagnostics,
    MethodRegistry methodRegistry,
    boolean warningsAsErrors,
    boolean emitDebugInfo
) {
    
    /**
     * Creates a context with sensible defaults.
     */
    public static CompilerContext defaults() {
        return builder().build();
    }
    
    /**
     * Creates a context for the given source file name.
     */
    public static CompilerContext forSource(String sourceName) {
        return builder().sourceName(sourceName).build();
    }
    
    /**
     * Starts building a custom context.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Creates a new context with a different source name.
     */
    public CompilerContext withSourceName(String newSourceName) {
        return new CompilerContext(
            newSourceName, output, diagnostics, methodRegistry, 
            warningsAsErrors, emitDebugInfo
        );
    }
    
    /**
     * Creates a fresh diagnostics collector (useful between compilations).
     */
    public CompilerContext withFreshDiagnostics() {
        return new CompilerContext(
            sourceName, output, new DiagnosticCollector(sourceName), 
            methodRegistry, warningsAsErrors, emitDebugInfo
        );
    }
    
    /**
     * Builder for CompilerContext.
     */
    public static class Builder {
        private String sourceName = "<input>";
        private PrintStream output = System.out;
        private DiagnosticCollector diagnostics;
        private MethodRegistry methodRegistry;
        private boolean warningsAsErrors = false;
        private boolean emitDebugInfo = false;
        
        public Builder sourceName(String sourceName) {
            this.sourceName = sourceName;
            return this;
        }
        
        public Builder output(PrintStream output) {
            this.output = output;
            return this;
        }
        
        public Builder diagnostics(DiagnosticCollector diagnostics) {
            this.diagnostics = diagnostics;
            return this;
        }
        
        public Builder methodRegistry(MethodRegistry registry) {
            this.methodRegistry = registry;
            return this;
        }
        
        public Builder warningsAsErrors(boolean value) {
            this.warningsAsErrors = value;
            return this;
        }
        
        public Builder emitDebugInfo(boolean value) {
            this.emitDebugInfo = value;
            return this;
        }
        
        public CompilerContext build() {
            // Create default diagnostics if not provided
            if (diagnostics == null) {
                diagnostics = new DiagnosticCollector(sourceName);
            }
            
            // Create default registry if not provided
            if (methodRegistry == null) {
                methodRegistry = new MethodRegistry();
                RangeBuiltins.register(methodRegistry);
            }
            
            return new CompilerContext(
                sourceName, output, diagnostics, methodRegistry,
                warningsAsErrors, emitDebugInfo
            );
        }
    }
}
