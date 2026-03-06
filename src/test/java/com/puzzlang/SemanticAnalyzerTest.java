package com.puzzlang;

import com.puzzlang.api.CompilerContext;
import com.puzzlang.api.PuzzCompiler;
import com.puzzlang.diagnostic.CompilationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for semantic analysis (scope checking, undefined variables, etc.)
 */
class SemanticAnalyzerTest {

    @Test
    void detectsUndefinedVariable() {
        String source = "print x\n";
        
        CompilerContext ctx = CompilerContext.defaults();
        PuzzCompiler compiler = new PuzzCompiler(ctx);
        
        CompilationException ex = assertThrows(CompilationException.class, () -> {
            compiler.compile(source, "Test");
        });
        
        assertTrue(ex.getMessage().contains("Undefined variable 'x'"));
    }

    @Test
    void allowsDefinedVariable() throws Exception {
        String source = """
            let x = 42
            print x
            """;
        
        PuzzCompiler compiler = new PuzzCompiler();
        // Should not throw
        assertDoesNotThrow(() -> compiler.compile(source, "Test"));
    }

    @Test
    void detectsUndefinedInExpression() {
        String source = """
            let x = y + 1
            print x
            """;
        
        CompilationException ex = assertThrows(CompilationException.class, () -> {
            new PuzzCompiler().compile(source, "Test");
        });
        
        assertTrue(ex.getMessage().contains("Undefined variable 'y'"));
    }

    @Test
    void detectsUndefinedInCondition() {
        String source = """
            if flag:
                print "yes"
            """;
        
        CompilationException ex = assertThrows(CompilationException.class, () -> {
            new PuzzCompiler().compile(source, "Test");
        });
        
        assertTrue(ex.getMessage().contains("Undefined variable 'flag'"));
    }

    @Test
    void forLoopVariableInScope() throws Exception {
        String source = """
            for i in 1..5:
                print i
            """;
        
        // Loop variable 'i' should be defined within the loop
        assertDoesNotThrow(() -> new PuzzCompiler().compile(source, "Test"));
    }

    @Test
    void forLoopVariableOutOfScope() {
        String source = """
            for i in 1..5:
                print i
            print i
            """;
        
        // 'i' should not be visible after the loop
        CompilationException ex = assertThrows(CompilationException.class, () -> {
            new PuzzCompiler().compile(source, "Test");
        });
        
        assertTrue(ex.getMessage().contains("Undefined variable 'i'"));
    }

    @Test
    void nestedScopes() throws Exception {
        String source = """
            let x = 1
            if true:
                let y = 2
                print x + y
            """;
        
        // Inner scope can see outer variable
        assertDoesNotThrow(() -> new PuzzCompiler().compile(source, "Test"));
    }

    @Test
    void innerVariableNotVisibleOutside() {
        String source = """
            if true:
                let inner = 42
            print inner
            """;
        
        CompilationException ex = assertThrows(CompilationException.class, () -> {
            new PuzzCompiler().compile(source, "Test");
        });
        
        assertTrue(ex.getMessage().contains("Undefined variable 'inner'"));
    }

    @Test
    void warningsForUnusedVariables() {
        String source = """
            let unused = 42
            print "hello"
            """;
        
        CompilerContext ctx = CompilerContext.defaults();
        PuzzCompiler compiler = new PuzzCompiler(ctx);
        
        // Should compile successfully (warnings don't fail by default)
        assertDoesNotThrow(() -> compiler.compile(source, "Test"));
        
        // But should have a warning
        assertTrue(compiler.getDiagnostics().hasWarnings());
        assertTrue(compiler.getDiagnostics().getWarnings().stream()
            .anyMatch(w -> w.message().contains("unused")));
    }

    @Test
    void warningsAsErrorsMode() {
        String source = """
            let unused = 42
            print "hello"
            """;
        
        CompilerContext ctx = CompilerContext.builder()
            .warningsAsErrors(true)
            .build();
        PuzzCompiler compiler = new PuzzCompiler(ctx);
        
        // Should fail when warnings are treated as errors
        assertThrows(CompilationException.class, () -> {
            compiler.compile(source, "Test");
        });
    }

    @Test
    void methodCallOnUndefinedVariable() {
        String source = "print unknown.sum()\n";
        
        CompilationException ex = assertThrows(CompilationException.class, () -> {
            new PuzzCompiler().compile(source, "Test");
        });
        
        assertTrue(ex.getMessage().contains("Undefined variable 'unknown'"));
    }

    @Test
    void rangeExpressionVariablesChecked() {
        String source = "print (start..end).sum()\n";
        
        CompilationException ex = assertThrows(CompilationException.class, () -> {
            new PuzzCompiler().compile(source, "Test");
        });
        
        // Should detect both undefined variables
        assertTrue(ex.getMessage().contains("Undefined variable"));
    }
}
