package com.puzzlang.runtime;

import java.util.*;

/**
 * Registry for runtime method dispatch.
 * 
 * Maps (Type, MethodName) → MethodHandler, allowing extensible method
 * definitions without modifying the core runtime.
 * 
 * Usage:
 *   MethodRegistry registry = new MethodRegistry();
 *   RangeBuiltins.register(registry);
 *   
 *   // Later, at runtime:
 *   Object result = registry.dispatch(receiver, "sum", new Object[]{});
 */
public class MethodRegistry {
    
    // Type → (MethodName → Handler)
    private final Map<Class<?>, Map<String, MethodHandler>> handlers = new HashMap<>();
    
    /**
     * Registers a method handler for a specific type.
     */
    public void register(Class<?> type, String method, MethodHandler handler) {
        handlers.computeIfAbsent(type, k -> new HashMap<>()).put(method, handler);
    }
    
    /**
     * Registers multiple methods for a type at once.
     */
    public void registerAll(Class<?> type, Map<String, MethodHandler> methods) {
        handlers.computeIfAbsent(type, k -> new HashMap<>()).putAll(methods);
    }
    
    /**
     * Dispatches a method call to the appropriate handler.
     * 
     * @param receiver The object on which to call the method
     * @param method The method name
     * @param args The method arguments
     * @return The result of the method call
     * @throws PuzzLangException if no handler is found
     */
    public Object dispatch(Object receiver, String method, Object[] args) {
        if (receiver == null) {
            throw new PuzzLangException("cannot call method '%s' on null", method);
        }
        
        // Try exact class match first
        MethodHandler handler = findHandler(receiver.getClass(), method);
        
        if (handler == null) {
            throw PuzzLangException.undefinedMethod(method, receiver);
        }
        
        return handler.invoke(receiver, args);
    }
    
    /**
     * Checks if a method exists for the given type.
     */
    public boolean hasMethod(Class<?> type, String method) {
        return findHandler(type, method) != null;
    }
    
    /**
     * Gets all registered method names for a type.
     */
    public Set<String> getMethodsFor(Class<?> type) {
        Set<String> methods = new HashSet<>();
        
        // Walk the class hierarchy
        Class<?> current = type;
        while (current != null) {
            Map<String, MethodHandler> typeHandlers = handlers.get(current);
            if (typeHandlers != null) {
                methods.addAll(typeHandlers.keySet());
            }
            current = current.getSuperclass();
        }
        
        // Also check interfaces
        for (Class<?> iface : type.getInterfaces()) {
            Map<String, MethodHandler> ifaceHandlers = handlers.get(iface);
            if (ifaceHandlers != null) {
                methods.addAll(ifaceHandlers.keySet());
            }
        }
        
        return methods;
    }
    
    // ── Private helpers ─────────────────────────────────────────────────────
    
    private MethodHandler findHandler(Class<?> type, String method) {
        // Walk the class hierarchy looking for a handler
        Class<?> current = type;
        while (current != null) {
            Map<String, MethodHandler> typeHandlers = handlers.get(current);
            if (typeHandlers != null) {
                MethodHandler handler = typeHandlers.get(method);
                if (handler != null) return handler;
            }
            current = current.getSuperclass();
        }
        
        // Check interfaces
        for (Class<?> iface : type.getInterfaces()) {
            Map<String, MethodHandler> ifaceHandlers = handlers.get(iface);
            if (ifaceHandlers != null) {
                MethodHandler handler = ifaceHandlers.get(method);
                if (handler != null) return handler;
            }
        }
        
        return null;
    }
}
