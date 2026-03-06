package com.puzzlang.runtime;

/**
 * Functional interface for handling method calls on runtime values.
 * 
 * Implementations receive the receiver object and an array of arguments,
 * and return the result of the method call.
 */
@FunctionalInterface
public interface MethodHandler {
    
    /**
     * Invokes the method on the given receiver with the provided arguments.
     * 
     * @param receiver The object on which the method is called
     * @param args The method arguments (may be empty)
     * @return The result of the method call
     * @throws PuzzLangException if the method call fails
     */
    Object invoke(Object receiver, Object[] args);
}
