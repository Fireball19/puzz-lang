package com.puzzlang.ast;

import java.util.List;

/**
 * Typed AST node hierarchy for PuzzLang.
 *
 * Uses Java 17 sealed interfaces + records for exhaustive pattern matching.
 *
 * NEW in this version:
 *   RangeLit       — expr..expr    (exclusive end)
 *   RangeInclusive — expr..=expr   (inclusive end)
 *   ForIn          — for x in expr: body
 *   MethodCall     — expr.method(args...)
 */
public class Nodes {

    // ── Statements ──────────────────────────────────────────────────────────

    public sealed interface Stmt
            permits Program, VarDecl, PrintStmt, IfStmt, WhileStmt, ForIn, ExprStmt {}

    public record Program(List<Stmt> stmts) implements Stmt {}
    public record VarDecl(String name, Expr value) implements Stmt {}
    public record PrintStmt(Expr value) implements Stmt {}
    public record IfStmt(Expr condition, List<Stmt> body, List<Stmt> elseBody) implements Stmt {}
    public record WhileStmt(Expr condition, List<Stmt> body) implements Stmt {}

    /**
     * for x in expr:
     *     body
     *
     * At runtime 'expr' must evaluate to an AocRange (or later a List).
     */
    public record ForIn(String var, Expr iterable, List<Stmt> body) implements Stmt {}

    public record ExprStmt(Expr expr) implements Stmt {}

    // ── Expressions ─────────────────────────────────────────────────────────

    public sealed interface Expr
            permits IntLit, StringLit, BoolLit, VarRef, BinOp,
            RangeLit, RangeInclusive, MethodCall {}

    public record IntLit(int value) implements Expr {}
    public record StringLit(String value) implements Expr {}
    public record BoolLit(boolean value) implements Expr {}
    public record VarRef(String name) implements Expr {}
    public record BinOp(Expr left, String op, Expr right) implements Expr {}

    /**
     * start..end  — exclusive: contains start, start+1, ..., end-1
     * Example: 1..5 → [1, 2, 3, 4]
     */
    public record RangeLit(Expr start, Expr end) implements Expr {}

    /**
     * start..=end — inclusive: contains start, start+1, ..., end
     * Example: 1..=5 → [1, 2, 3, 4, 5]
     */
    public record RangeInclusive(Expr start, Expr end) implements Expr {}

    /**
     * receiver.method(arg0, arg1, ...)
     * Examples:
     *   (1..10).sum()
     *   (1..100).contains(42)
     *   myRange.overlaps(other)
     */
    public record MethodCall(Expr receiver, String method, List<Expr> args) implements Expr {}
}