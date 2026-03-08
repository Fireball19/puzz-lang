package com.puzzlang.ast;

import java.util.List;

/**
 * Typed AST node hierarchy for PuzzLang.
 *
 * Uses Java 17 sealed interfaces + records for exhaustive pattern matching.
 * All nodes now carry SourceLocation for precise error reporting.
 *
 * Node types:
 *   Statements: Program, VarDecl, PrintStmt, IfStmt, WhileStmt, ForIn, MatchStmt, ExprStmt
 *   Expressions: IntLit, StringLit, BoolLit, VarRef, BinOp, RangeLit, RangeInclusive, 
 *                MethodCall, FunctionCall, StdinCall, ReadCall, TupleLit, ListLit, ListComprehension
 *   Patterns: StringPattern, TuplePattern, WildcardPattern
 *   Destructure Targets: SingleTarget, TupleTarget
 */
public class Nodes {

    // ── Statements ──────────────────────────────────────────────────────────

    public sealed interface Stmt permits Program, VarDecl, PrintStmt, IfStmt, WhileStmt, ForIn, MatchStmt, ExprStmt {
        SourceLocation location();
    }

    public record Program(List<Stmt> stmts, SourceLocation location) implements Stmt {
        public Program(List<Stmt> stmts) {
            this(stmts, stmts.isEmpty() ? SourceLocation.UNKNOWN : stmts.get(0).location());
        }
    }

    /**
     * Variable declaration with optional destructuring:
     *   let x = 5                    → SingleTarget("x")
     *   let (x, y) = point           → TupleTarget(["x", "y"])
     *   let a, b = b, a              → TupleTarget(["a", "b"])
     */
    public record VarDecl(DestructureTarget target, Expr value, SourceLocation location) implements Stmt {
        // Convenience constructor for single variable (backwards compatible)
        public VarDecl(String name, Expr value, SourceLocation location) {
            this(new SingleTarget(name, location), value, location);
        }
        
        public VarDecl(String name, Expr value) {
            this(name, value, SourceLocation.UNKNOWN);
        }
        
        // For backwards compatibility - returns the name if single target
        public String name() {
            return target instanceof SingleTarget st ? st.name() : null;
        }
        
        // Check if this is a simple single-variable declaration
        public boolean isSimple() {
            return target instanceof SingleTarget;
        }
    }

    public record PrintStmt(Expr value, SourceLocation location) implements Stmt {
        public PrintStmt(Expr value) {
            this(value, SourceLocation.UNKNOWN);
        }
    }

    public record IfStmt(Expr condition, List<Stmt> body, List<Stmt> elseBody, SourceLocation location) implements Stmt {
        public IfStmt(Expr condition, List<Stmt> body, List<Stmt> elseBody) {
            this(condition, body, elseBody, SourceLocation.UNKNOWN);
        }
    }

    public record WhileStmt(Expr condition, List<Stmt> body, SourceLocation location) implements Stmt {
        public WhileStmt(Expr condition, List<Stmt> body) {
            this(condition, body, SourceLocation.UNKNOWN);
        }
    }

    /**
     * for target in expr:
     *     body
     *
     * Target can be:
     *   - SingleTarget: for x in items
     *   - TupleTarget: for (x, y) in points
     */
    public record ForIn(DestructureTarget target, Expr iterable, List<Stmt> body, SourceLocation location) implements Stmt {
        // Convenience constructor for single variable (backwards compatible)
        public ForIn(String var, Expr iterable, List<Stmt> body, SourceLocation location) {
            this(new SingleTarget(var, location), iterable, body, location);
        }
        
        public ForIn(String var, Expr iterable, List<Stmt> body) {
            this(var, iterable, body, SourceLocation.UNKNOWN);
        }
        
        // For backwards compatibility
        public String var() {
            return target instanceof SingleTarget st ? st.name() : null;
        }
        
        public boolean isSimple() {
            return target instanceof SingleTarget;
        }
    }

    /**
     * match expr:
     *     "pattern {x}" -> stmt
     *     (0, 0) -> stmt
     *     (x, y) -> stmt
     *     _ -> stmt
     *
     * Pattern matching with destructuring for string parsing and tuples.
     */
    public record MatchStmt(Expr subject, List<MatchArm> arms, SourceLocation location) implements Stmt {
        public MatchStmt(Expr subject, List<MatchArm> arms) {
            this(subject, arms, SourceLocation.UNKNOWN);
        }
    }

    public record ExprStmt(Expr expr, SourceLocation location) implements Stmt {
        public ExprStmt(Expr expr) {
            this(expr, expr.location());
        }
    }

    // ── Destructure Targets ─────────────────────────────────────────────────

    /**
     * Target for destructuring assignment in let statements and for loops.
     */
    public sealed interface DestructureTarget permits SingleTarget, TupleTarget {
        SourceLocation location();
        List<String> names();  // All variable names being bound
    }

    /**
     * Single variable target: let x = 5, for x in items
     */
    public record SingleTarget(String name, SourceLocation location) implements DestructureTarget {
        public SingleTarget(String name) {
            this(name, SourceLocation.UNKNOWN);
        }
        
        @Override
        public List<String> names() {
            return List.of(name);
        }
    }

    /**
     * Tuple destructuring target: let (x, y) = point, for (a, b) in pairs
     */
    public record TupleTarget(List<String> names, SourceLocation location) implements DestructureTarget {
        public TupleTarget(List<String> names) {
            this(names, SourceLocation.UNKNOWN);
        }
        
        public int size() {
            return names.size();
        }
    }

    // ── Match Arms and Patterns ─────────────────────────────────────────────

    /**
     * A single arm in a match statement: pattern -> body
     */
    public record MatchArm(MatchPattern pattern, Stmt body, SourceLocation location) {
        public MatchArm(MatchPattern pattern, Stmt body) {
            this(pattern, body, pattern.location());
        }
    }

    /**
     * Patterns used in match arms.
     */
    public sealed interface MatchPattern permits StringPattern, TuplePattern, WildcardPattern {
        SourceLocation location();
    }

    /**
     * String pattern with captures: "move {n} from {a} to {b}"
     * The template is the raw string, captureNames are extracted placeholder names.
     */
    public record StringPattern(String template, List<String> captureNames, SourceLocation location) implements MatchPattern {
        public StringPattern(String template, List<String> captureNames) {
            this(template, captureNames, SourceLocation.UNKNOWN);
        }
    }

    /**
     * Tuple pattern: (0, 0), (x, 0), (x, y), (_, y)
     * Each element can be:
     *   - LiteralElement: matches exact value (0, "hello")
     *   - BindingElement: captures to variable (x, y)
     *   - WildcardElement: matches anything, no capture (_)
     */
    public record TuplePattern(List<TuplePatternElement> elements, SourceLocation location) implements MatchPattern {
        public TuplePattern(List<TuplePatternElement> elements) {
            this(elements, SourceLocation.UNKNOWN);
        }
        
        public int size() {
            return elements.size();
        }
        
        /**
         * Returns list of variable names that this pattern binds.
         */
        public List<String> captureNames() {
            return elements.stream()
                .filter(e -> e instanceof BindingElement)
                .map(e -> ((BindingElement) e).name())
                .toList();
        }
    }

    /**
     * Elements within a tuple pattern.
     */
    public sealed interface TuplePatternElement permits LiteralElement, BindingElement, WildcardElement {
    }

    /**
     * Literal value in tuple pattern: matches exact value.
     * Examples: (0, _), ("start", x)
     */
    public record LiteralElement(Object value) implements TuplePatternElement {
    }

    /**
     * Variable binding in tuple pattern: captures the value.
     * Examples: (x, y), (a, b, c)
     */
    public record BindingElement(String name) implements TuplePatternElement {
    }

    /**
     * Wildcard in tuple pattern: matches anything, doesn't capture.
     * Example: (x, _)
     */
    public record WildcardElement() implements TuplePatternElement {
    }

    /**
     * Wildcard pattern: _ (matches anything, no captures)
     */
    public record WildcardPattern(SourceLocation location) implements MatchPattern {
        public WildcardPattern() {
            this(SourceLocation.UNKNOWN);
        }
    }

    // ── Expressions ─────────────────────────────────────────────────────────

    public sealed interface Expr permits IntLit, StringLit, BoolLit, VarRef, BinOp,
            RangeLit, RangeInclusive, MethodCall, FunctionCall, StdinCall, ReadCall,
            TupleLit, ListLit, ListComprehension {
        SourceLocation location();
    }

    public record IntLit(int value, SourceLocation location) implements Expr {
        public IntLit(int value) {
            this(value, SourceLocation.UNKNOWN);
        }
    }

    public record StringLit(String value, SourceLocation location) implements Expr {
        public StringLit(String value) {
            this(value, SourceLocation.UNKNOWN);
        }
    }

    public record BoolLit(boolean value, SourceLocation location) implements Expr {
        public BoolLit(boolean value) {
            this(value, SourceLocation.UNKNOWN);
        }
    }

    public record VarRef(String name, SourceLocation location) implements Expr {
        public VarRef(String name) {
            this(name, SourceLocation.UNKNOWN);
        }
    }

    public record BinOp(Expr left, String op, Expr right, SourceLocation location) implements Expr {
        public BinOp(Expr left, String op, Expr right) {
            this(left, op, right, left.location());
        }
    }

    /**
     * start..end — exclusive: contains start, start+1, ..., end-1
     * Example: 1..5 → [1, 2, 3, 4]
     */
    public record RangeLit(Expr start, Expr end, SourceLocation location) implements Expr {
        public RangeLit(Expr start, Expr end) {
            this(start, end, start.location());
        }
    }

    /**
     * start..=end — inclusive: contains start, start+1, ..., end
     * Example: 1..=5 → [1, 2, 3, 4, 5]
     */
    public record RangeInclusive(Expr start, Expr end, SourceLocation location) implements Expr {
        public RangeInclusive(Expr start, Expr end) {
            this(start, end, start.location());
        }
    }

    /**
     * receiver.method(arg0, arg1, ...)
     * Examples:
     *   (1..10).sum()
     *   (1..100).contains(42)
     *   myRange.overlaps(other)
     *   "hello\nworld".lines()
     */
    public record MethodCall(Expr receiver, String method, List<Expr> args, SourceLocation location) implements Expr {
        public MethodCall(Expr receiver, String method, List<Expr> args) {
            this(receiver, method, args, receiver.location());
        }
    }

    /**
     * name(arg0, arg1, ...) — global function call
     * Examples:
     *   gcd(12, 8)
     *   lcm(4, 6)
     *   primes_up_to(100)
     *   abs(-5)
     */
    public record FunctionCall(String name, List<Expr> args, SourceLocation location) implements Expr {
        public FunctionCall(String name, List<Expr> args) {
            this(name, args, SourceLocation.UNKNOWN);
        }
    }

    /**
     * stdin() — read all of standard input as a string.
     * Used for AoC-style input handling.
     */
    public record StdinCall(SourceLocation location) implements Expr {
        public StdinCall() {
            this(SourceLocation.UNKNOWN);
        }
    }

    /**
     * read(path) — read file contents as a string.
     * Example: read("input.txt")
     */
    public record ReadCall(Expr path, SourceLocation location) implements Expr {
        public ReadCall(Expr path) {
            this(path, path.location());
        }
    }

    /**
     * Tuple literal: (1, 2), (a, b, c), (x, y)
     * 
     * At runtime represented as PuzzTuple.
     */
    public record TupleLit(List<Expr> elements, SourceLocation location) implements Expr {
        public TupleLit(List<Expr> elements) {
            this(elements, elements.isEmpty() ? SourceLocation.UNKNOWN : elements.get(0).location());
        }
        
        public int size() {
            return elements.size();
        }
    }

    /**
     * List literal: [], [1, 2, 3], [a, b]
     * 
     * At runtime represented as PuzzList.
     */
    public record ListLit(List<Expr> elements, SourceLocation location) implements Expr {
        public ListLit(List<Expr> elements) {
            this(elements, SourceLocation.UNKNOWN);
        }
        
        public int size() {
            return elements.size();
        }
        
        public boolean isEmpty() {
            return elements.isEmpty();
        }
    }

    /**
     * List comprehension: [expr for target in iterable], [expr for target in iterable if cond]
     * 
     * Examples:
     *   [x * x for x in 1..=10]
     *   [x for x in nums if x % 2 == 0]
     *   [(x, y) for x in 1..=3 for y in 1..=3]
     */
    public record ListComprehension(
            Expr element,
            DestructureTarget target,
            Expr iterable,
            Expr condition,  // null if no condition
            // For nested comprehensions
            DestructureTarget secondTarget,  // null if not nested
            Expr secondIterable,             // null if not nested
            SourceLocation location
    ) implements Expr {
        
        // Simple comprehension: [x for x in items]
        public ListComprehension(Expr element, DestructureTarget target, Expr iterable, SourceLocation location) {
            this(element, target, iterable, null, null, null, location);
        }
        
        // Comprehension with condition: [x for x in items if x > 0]
        public ListComprehension(Expr element, DestructureTarget target, Expr iterable, Expr condition, SourceLocation location) {
            this(element, target, iterable, condition, null, null, location);
        }
        
        // Nested comprehension: [(x, y) for x in xs for y in ys]
        public ListComprehension(Expr element, DestructureTarget target, Expr iterable,
                                 DestructureTarget secondTarget, Expr secondIterable, SourceLocation location) {
            this(element, target, iterable, null, secondTarget, secondIterable, location);
        }
        
        public boolean hasCondition() {
            return condition != null;
        }
        
        public boolean isNested() {
            return secondTarget != null;
        }
    }
}
