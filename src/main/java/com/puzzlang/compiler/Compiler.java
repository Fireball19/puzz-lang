package com.puzzlang.compiler;

import com.puzzlang.ast.Nodes.Program;
import com.puzzlang.parser.PuzzLangLexer;
import com.puzzlang.parser.PuzzLangParser;
import org.antlr.v4.runtime.*;

import java.lang.reflect.Method;

public class Compiler {

    public byte[] compile(String source, String className) {
        String processed = IndentPreprocessor.process(source);

        CharStream chars   = CharStreams.fromString(processed);
        PuzzLangLexer lexer = new PuzzLangLexer(chars);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new DiagnosticErrorListener());

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PuzzLangParser parser     = new PuzzLangParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new DiagnosticErrorListener());

        PuzzLangParser.ProgramContext tree = parser.program();

        AstBuilder builder = new AstBuilder();
        Program ast = builder.visitProgram(tree);

        BytecodeEmitter emitter = new BytecodeEmitter(className);
        return emitter.emit(ast);
    }

    public void execute(byte[] bytecode, String className) throws Exception {
        InMemoryClassLoader loader = new InMemoryClassLoader(className, bytecode);
        Class<?> clazz = loader.loadClass(className);
        Method main = clazz.getMethod("main", String[].class);
        main.invoke(null, (Object) new String[0]);
    }

    private static class InMemoryClassLoader extends ClassLoader {
        private final String name;
        private final byte[] bytes;
        InMemoryClassLoader(String name, byte[] bytes) {
            super(Compiler.class.getClassLoader());
            this.name = name; this.bytes = bytes;
        }
        @Override
        protected Class<?> findClass(String n) throws ClassNotFoundException {
            if (n.equals(name)) return defineClass(n, bytes, 0, bytes.length);
            return super.findClass(n);
        }
    }
}