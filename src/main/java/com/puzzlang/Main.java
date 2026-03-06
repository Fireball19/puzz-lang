package com.puzzlang;

import com.puzzlang.api.CompilerContext;
import com.puzzlang.api.CompiledUnit;
import com.puzzlang.api.PuzzCompiler;
import com.puzzlang.diagnostic.CompilationException;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Command-line entry point for PuzzLang.
 *
 * Usage:
 *   java -jar puzzlang.jar                    # Run demo
 *   java -jar puzzlang.jar script.puzz        # Run a script
 *   java -jar puzzlang.jar --check script.puzz # Check without running
 */
public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            runDemo();
            return;
        }

        String filename = args[args.length - 1];
        boolean checkOnly = args.length > 1 && "--check".equals(args[0]);

        runFile(filename, checkOnly);
    }

    private static void runFile(String filename, boolean checkOnly) throws Exception {
        Path path = Path.of(filename);
        if (!Files.exists(path)) {
            System.err.println("Error: File not found: " + filename);
            System.exit(1);
        }

        String source = Files.readString(path);
        String className = path.getFileName().toString()
                .replace(".puzz", "")
                .replace("-", "_");

        CompilerContext ctx = CompilerContext.forSource(filename);
        PuzzCompiler compiler = new PuzzCompiler(ctx);

        try {
            CompiledUnit unit = compiler.compile(source, className);

            // Print warnings even on success
            if (!compiler.getDiagnostics().isEmpty()) {
                compiler.getDiagnostics().printAll(System.err);
            }

            if (checkOnly) {
                System.out.println("✓ " + filename + " OK");
            } else {
                unit.execute();
            }
        } catch (CompilationException e) {
            compiler.getDiagnostics().printAll(System.err);
            System.exit(1);
        }
    }

    private static void runDemo() throws Exception {
        String source = """
            # Ranges demo
            let r = 1..5
            print r
            print r.sum()
            print r.count()
            print r.min()
            print r.max()
            print r.contains(3)
            print r.contains(5)
            
            # Gauss sum
            print (1..=100).sum()
            
            # For loop
            let total = 0
            for x in 1..=5:
                let total = total + x
            print total
            """;

        System.out.println("=== PuzzLang Demo ===\n");
        System.out.println("Source:");
        System.out.println("-------");
        System.out.println(source);
        System.out.println("Output:");
        System.out.println("-------");

        PuzzCompiler compiler = new PuzzCompiler();
        compiler.compileAndRun(source, "Demo");
    }
}
