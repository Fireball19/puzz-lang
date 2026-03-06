package com.puzzlang.api;

import java.lang.reflect.Method;

/**
 * Represents a compiled PuzzLang program ready for execution.
 * 
 * Encapsulates the class name and bytecode, providing a clean
 * interface for running the program.
 */
public record CompiledUnit(String className, byte[] bytecode) {
    
    /**
     * Executes the compiled program.
     * 
     * @throws Exception if execution fails
     */
    public void execute() throws Exception {
        execute(new String[0]);
    }
    
    /**
     * Executes the compiled program with command-line arguments.
     * 
     * @param args Command-line arguments to pass to main()
     * @throws Exception if execution fails
     */
    public void execute(String[] args) throws Exception {
        BytecodeClassLoader loader = new BytecodeClassLoader(className, bytecode);
        Class<?> clazz = loader.loadClass(className);
        Method main = clazz.getMethod("main", String[].class);
        main.invoke(null, (Object) args);
    }
    
    /**
     * Loads the compiled class without executing it.
     * Useful for reflection or custom invocation patterns.
     */
    public Class<?> loadClass() throws ClassNotFoundException {
        BytecodeClassLoader loader = new BytecodeClassLoader(className, bytecode);
        return loader.loadClass(className);
    }
    
    /**
     * Returns the bytecode size in bytes.
     */
    public int size() {
        return bytecode.length;
    }
    
    // ── Inner: ClassLoader ──────────────────────────────────────────────────
    
    private static class BytecodeClassLoader extends ClassLoader {
        private final String name;
        private final byte[] bytes;
        
        BytecodeClassLoader(String name, byte[] bytes) {
            super(CompiledUnit.class.getClassLoader());
            this.name = name;
            this.bytes = bytes;
        }
        
        @Override
        protected Class<?> findClass(String n) throws ClassNotFoundException {
            if (n.equals(name)) {
                return defineClass(n, bytes, 0, bytes.length);
            }
            return super.findClass(n);
        }
    }
}
