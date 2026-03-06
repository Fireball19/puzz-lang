package com.puzzlang.api;

import com.puzzlang.analysis.SemanticAnalyzer;
import com.puzzlang.ast.Nodes.Program;
import com.puzzlang.frontend.AstBuilder;
import com.puzzlang.backend.BytecodeEmitter;
import com.puzzlang.frontend.IndentPreprocessor;
import com.puzzlang.diagnostic.CompilationException;
import com.puzzlang.diagnostic.DiagnosticCollector;
import com.puzzlang.parser.PuzzLangLexer;
import com.puzzlang.parser.PuzzLangParser;
import org.antlr.v4.runtime.*;

/**
 * Main entry point for the PuzzLang compiler.
 * 
 * Orchestrates the compilation pipeline:
 *   1. Preprocessing (indent → tokens)
 *   2. Lexing/Parsing (source → parse tree)
 *   3. AST Building (parse tree → typed AST)
 *   4. Semantic Analysis (scope checking, validation)
 *   5. Code Generation (AST → JVM bytecode)
 * 
 * Usage:
 *   PuzzCompiler compiler = new PuzzCompiler();
 *   CompiledUnit unit = compiler.compile(source, "MyProgram");
 *   unit.execute();
 * 
 * Or with custom context:
 *   CompilerContext ctx = CompilerContext.forSource("myfile.puzz");
 *   PuzzCompiler compiler = new PuzzCompiler(ctx);
 *   CompiledUnit unit = compiler.compile(source, "MyProgram");
 */
public class PuzzCompiler {
    
    private final CompilerContext context;
    
    /**
     * Creates a compiler with default settings.
     */
    public PuzzCompiler() {
        this(CompilerContext.defaults());
    }
    
    /**
     * Creates a compiler with custom context.
     */
    public PuzzCompiler(CompilerContext context) {
        this.context = context;
    }
    
    /**
     * Compiles source code to bytecode.
     * 
     * @param source The PuzzLang source code
     * @param className The name for the generated class
     * @return A CompiledUnit ready for execution
     * @throws CompilationException if compilation fails
     */
    public CompiledUnit compile(String source, String className) {
        DiagnosticCollector diagnostics = context.diagnostics();
        
        // 1. Preprocessing
        String preprocessed = IndentPreprocessor.process(source);
        
        // 2. Lexing/Parsing
        PuzzLangParser.ProgramContext parseTree = parse(preprocessed, diagnostics);
        diagnostics.throwIfErrors();
        
        // 3. AST Building
        AstBuilder builder = new AstBuilder();
        Program ast = builder.visitProgram(parseTree);
        
        // 4. Semantic Analysis
        SemanticAnalyzer analyzer = new SemanticAnalyzer(diagnostics);
        analyzer.analyze(ast);
        
        // Check for errors (warnings are ok)
        if (context.warningsAsErrors() && diagnostics.hasWarnings()) {
            throw diagnostics.toException();
        }
        diagnostics.throwIfErrors();
        
        // 5. Code Generation
        BytecodeEmitter emitter = new BytecodeEmitter(className);
        byte[] bytecode = emitter.emit(ast);
        
        return new CompiledUnit(className, bytecode);
    }
    
    /**
     * Compiles and immediately executes source code.
     * 
     * @param source The PuzzLang source code
     * @param className The name for the generated class
     * @throws Exception if compilation or execution fails
     */
    public void compileAndRun(String source, String className) throws Exception {
        compile(source, className).execute();
    }
    
    /**
     * Gets the compiler context.
     */
    public CompilerContext getContext() {
        return context;
    }
    
    /**
     * Gets the diagnostics collector.
     */
    public DiagnosticCollector getDiagnostics() {
        return context.diagnostics();
    }
    
    // ── Private: Parsing ────────────────────────────────────────────────────
    
    private PuzzLangParser.ProgramContext parse(String source, DiagnosticCollector diagnostics) {
        CharStream chars = CharStreams.fromString(source);
        
        PuzzLangLexer lexer = new PuzzLangLexer(chars);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new DiagnosticErrorListener(diagnostics));
        
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        
        PuzzLangParser parser = new PuzzLangParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new DiagnosticErrorListener(diagnostics));
        
        return parser.program();
    }
    
    // ── Inner: Error Listener ───────────────────────────────────────────────
    
    private static class DiagnosticErrorListener extends BaseErrorListener {
        private final DiagnosticCollector diagnostics;
        
        DiagnosticErrorListener(DiagnosticCollector diagnostics) {
            this.diagnostics = diagnostics;
        }
        
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer,
                                Object offendingSymbol,
                                int line, int charPositionInLine,
                                String msg,
                                RecognitionException e) {
            diagnostics.error(
                com.puzzlang.ast.SourceLocation.at(line, charPositionInLine + 1),
                "P001",
                "Syntax error: %s", msg
            );
        }
    }
}
