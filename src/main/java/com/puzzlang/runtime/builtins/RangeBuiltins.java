package com.puzzlang.runtime.builtins;

import com.puzzlang.runtime.*;

/**
 * Built-in methods for PuzzRange and SteppedRange types.
 * 
 * Registers all range-related methods with the MethodRegistry.
 * This pattern allows adding new types without modifying PuzzRuntime.
 */
public class RangeBuiltins {
    
    private RangeBuiltins() {} // Static utility class
    
    /**
     * Registers all range methods with the given registry.
     */
    public static void register(MethodRegistry registry) {
        registerRangeMethods(registry);
        registerSteppedRangeMethods(registry);
    }
    
    private static void registerRangeMethods(MethodRegistry registry) {
        Class<PuzzRange> type = PuzzRange.class;
        
        // Aggregation methods
        registry.register(type, "sum", (r, args) -> ((PuzzRange) r).sum());
        registry.register(type, "product", (r, args) -> ((PuzzRange) r).product());
        registry.register(type, "count", (r, args) -> (long) ((PuzzRange) r).count());
        registry.register(type, "min", (r, args) -> ((PuzzRange) r).min());
        registry.register(type, "max", (r, args) -> ((PuzzRange) r).max());
        
        // Inspection methods
        registry.register(type, "isEmpty", (r, args) -> ((PuzzRange) r).isEmpty());
        registry.register(type, "isInclusive", (r, args) -> ((PuzzRange) r).inclusive());
        registry.register(type, "start", (r, args) -> ((PuzzRange) r).startValue());
        registry.register(type, "end", (r, args) -> ((PuzzRange) r).lastValue());
        
        // Membership methods
        registry.register(type, "contains", (r, args) -> {
            requireArgs("contains", args, 1);
            return ((PuzzRange) r).contains(PuzzRuntime.toInt(args[0]));
        });
        
        // Set operations
        registry.register(type, "overlaps", (r, args) -> {
            requireArgs("overlaps", args, 1);
            if (!(args[0] instanceof PuzzRange other)) {
                throw new PuzzLangException("overlaps() requires a range argument");
            }
            return ((PuzzRange) r).overlaps(other);
        });
        
        registry.register(type, "intersection", (r, args) -> {
            requireArgs("intersection", args, 1);
            if (!(args[0] instanceof PuzzRange other)) {
                throw new PuzzLangException("intersection() requires a range argument");
            }
            PuzzRange result = ((PuzzRange) r).intersection(other);
            return result != null ? result : "null";
        });
        
        // Transformation methods
        registry.register(type, "step", (r, args) -> {
            requireArgs("step", args, 1);
            return ((PuzzRange) r).step(PuzzRuntime.toInt(args[0]));
        });
        
        registry.register(type, "toList", (r, args) -> {
            var list = new java.util.ArrayList<Integer>();
            ((PuzzRange) r).forEach(list::add);
            return list;
        });
    }
    
    private static void registerSteppedRangeMethods(MethodRegistry registry) {
        Class<PuzzRange.SteppedRange> type = PuzzRange.SteppedRange.class;
        
        registry.register(type, "sum", (r, args) -> ((PuzzRange.SteppedRange) r).sum());
        registry.register(type, "count", (r, args) -> (long) ((PuzzRange.SteppedRange) r).count());
    }
    
    // ── Helper ──────────────────────────────────────────────────────────────
    
    private static void requireArgs(String method, Object[] args, int expected) {
        if (args.length != expected) {
            throw PuzzLangException.argumentCount(method, expected, args.length);
        }
    }
}
