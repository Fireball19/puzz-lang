package com.puzzlang.ast;

import java.util.List;

/**
 * Typed AST node hierarchy for PuzzLang.
 *
 * Uses Java 17 sealed interfaces + records for exhaustive pattern matching.
 * All nodes now carry SourceLocation for precise error reporting.
 *
 * Node types:
 *   Statements: Program, VarDecl, PrintStmt, IfStmt, WhileStmt, ForIn, ExprStmt
 *   Expressions: IntLit, StringLit, BoolLit, VarRef, BinOp, RangeLit, RangeInclusive, MethodCall
 */
public class Nodes {

    // ── Statements ──────────────────────────────────────────────────────────

    public sealed interface Stmt permits Program, VarDecl, PrintStmt, IfStmt, WhileStmt, ForIn, ExprStmt {
        SourceLocation location();
    }

    public record Program(List<Stmt> stmts, SourceLocation location) implements Stmt {
        public Program(List<Stmt> stmts) {
            this(stmts, stmts.isEmpty() ? SourceLocation.UNKNOWN : stmts.get(0).location());
        }
    }
    
    public record VarDecl(String name, Expr value, SourceLocation location) implements Stmt {
        public VarDecl(String name, Expr value) {
            this(name, value, SourceLocation.UNKNOWN);
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
     * for x in expr:
     *     body
     *
     * At runtime 'expr' must evaluate to an iterable (ranges, later lists).
     */
    public record ForIn(String var, Expr iterable, List<Stmt> body, SourceLocation location) implements Stmt {
        public ForIn(String var, Expr iterable, List<Stmt> body) {
            this(var, iterable, body, SourceLocation.UNKNOWN);
        }
    }

    public record ExprStmt(Expr expr, SourceLocation location) implements Stmt {
        public ExprStmt(Expr expr) {
            this(expr, expr.location());
        }
    }

    // ── Expressions ─────────────────────────────────────────────────────────

    public sealed interface Expr permits IntLit, StringLit, BoolLit, VarRef, BinOp,
            RangeLit, RangeInclusive, MethodCall {
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
     */
    public record MethodCall(Expr receiver, String method, List<Expr> args, SourceLocation location) implements Expr {
        public MethodCall(Expr receiver, String method, List<Expr> args) {
            this(receiver, method, args, receiver.location());
        }
    }
}
