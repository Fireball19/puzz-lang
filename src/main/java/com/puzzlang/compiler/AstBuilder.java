package com.puzzlang.compiler;

import com.puzzlang.ast.Nodes.*;
import com.puzzlang.parser.PuzzLangBaseVisitor;
import com.puzzlang.parser.PuzzLangParser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Walks the ANTLR4 parse tree and builds our typed AST.
 *
 * New visitor methods for ranges:
 *   visitRangeLit        — expr..expr
 *   visitRangeInclusive  — expr..=expr
 *   visitMethodCall      — expr.method(args)
 *   visitForInStmt       — for x in expr: body
 */
public class AstBuilder extends PuzzLangBaseVisitor<Object> {

    @Override
    public Program visitProgram(PuzzLangParser.ProgramContext ctx) {
        List<Stmt> stmts = ctx.statement().stream()
                .map(this::visitStatement)
                .collect(Collectors.toList());
        return new Program(stmts);
    }

    @Override
    public Stmt visitStatement(PuzzLangParser.StatementContext ctx) {
        if (ctx.varDecl()    != null) return visitVarDecl(ctx.varDecl());
        if (ctx.printStmt()  != null) return visitPrintStmt(ctx.printStmt());
        if (ctx.ifStmt()     != null) return visitIfStmt(ctx.ifStmt());
        if (ctx.whileStmt()  != null) return visitWhileStmt(ctx.whileStmt());
        if (ctx.forInStmt()  != null) return visitForInStmt(ctx.forInStmt());
        return visitExprStmt(ctx.exprStmt());
    }

    @Override
    public VarDecl visitVarDecl(PuzzLangParser.VarDeclContext ctx) {
        return new VarDecl(ctx.ID().getText(), visitExprCtx(ctx.expr()));
    }

    @Override
    public PrintStmt visitPrintStmt(PuzzLangParser.PrintStmtContext ctx) {
        return new PrintStmt(visitExprCtx(ctx.expr()));
    }

    @Override
    public IfStmt visitIfStmt(PuzzLangParser.IfStmtContext ctx) {
        Expr cond = visitExprCtx(ctx.expr());
        List<PuzzLangParser.StatementContext> all = ctx.statement();
        int elseIdx = findElseIndex(ctx);
        List<Stmt> body     = elseIdx < 0
                ? mapStmts(all, 0, all.size())
                : mapStmts(all, 0, elseIdx);
        List<Stmt> elseBody = elseIdx < 0
                ? List.of()
                : mapStmts(all, elseIdx, all.size());
        return new IfStmt(cond, body, elseBody);
    }

    @Override
    public WhileStmt visitWhileStmt(PuzzLangParser.WhileStmtContext ctx) {
        Expr cond = visitExprCtx(ctx.expr());
        List<Stmt> body = ctx.statement().stream()
                .map(this::visitStatement).collect(Collectors.toList());
        return new WhileStmt(cond, body);
    }

    @Override
    public ForIn visitForInStmt(PuzzLangParser.ForInStmtContext ctx) {
        String varName = ctx.ID().getText();
        Expr iterable  = visitExprCtx(ctx.expr());
        List<Stmt> body = ctx.statement().stream()
                .map(this::visitStatement).collect(Collectors.toList());
        return new ForIn(varName, iterable, body);
    }

    @Override
    public ExprStmt visitExprStmt(PuzzLangParser.ExprStmtContext ctx) {
        return new ExprStmt(visitExprCtx(ctx.expr()));
    }

    public Expr visitExprCtx(PuzzLangParser.ExprContext ctx) {
        if (ctx instanceof PuzzLangParser.IntLitContext c)
            return new IntLit(Integer.parseInt(c.INT().getText()));

        if (ctx instanceof PuzzLangParser.StringLitContext c) {
            String raw = c.STRING().getText();
            return new StringLit(raw.substring(1, raw.length() - 1));
        }
        if (ctx instanceof PuzzLangParser.BoolLitContext c)
            return new BoolLit(c.BOOL().getText().equals("true"));

        if (ctx instanceof PuzzLangParser.VarContext c)
            return new VarRef(c.ID().getText());

        if (ctx instanceof PuzzLangParser.ParensContext c)
            return visitExprCtx(c.expr());

        if (ctx instanceof PuzzLangParser.MulDivContext c)
            return new BinOp(visitExprCtx(c.expr(0)), c.op.getText(), visitExprCtx(c.expr(1)));

        if (ctx instanceof PuzzLangParser.AddSubContext c)
            return new BinOp(visitExprCtx(c.expr(0)), c.op.getText(), visitExprCtx(c.expr(1)));

        if (ctx instanceof PuzzLangParser.CompareContext c)
            return new BinOp(visitExprCtx(c.expr(0)), c.op.getText(), visitExprCtx(c.expr(1)));

        // ── Range literals ─────────────────────────────────────────────────
        if (ctx instanceof PuzzLangParser.RangeLitContext c)
            return new RangeLit(visitExprCtx(c.expr(0)), visitExprCtx(c.expr(1)));

        if (ctx instanceof PuzzLangParser.RangeInclusiveContext c)
            return new RangeInclusive(visitExprCtx(c.expr(0)), visitExprCtx(c.expr(1)));

        // ── Method calls ───────────────────────────────────────────────────
        if (ctx instanceof PuzzLangParser.MethodCallContext c) {
            Expr receiver  = visitExprCtx(c.expr(0));
            String method  = c.method.getText();
            // args: expr(1)..expr(n)  (expr(0) is the receiver)
            List<Expr> args = new ArrayList<>();
            for (int i = 1; i < c.expr().size(); i++) {
                args.add(visitExprCtx(c.expr(i)));
            }
            return new MethodCall(receiver, method, args);
        }

        throw new RuntimeException("Unknown expr: " + ctx.getClass().getSimpleName());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private List<Stmt> mapStmts(List<PuzzLangParser.StatementContext> all, int from, int to) {
        List<Stmt> result = new ArrayList<>();
        for (int i = from; i < to; i++) result.add(visitStatement(all.get(i)));
        return result;
    }

    private int findElseIndex(PuzzLangParser.IfStmtContext ctx) {
        int stmtCount = 0;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            var child = ctx.getChild(i);
            if (child instanceof PuzzLangParser.StatementContext) stmtCount++;
            else if ("else".equals(child.getText())) return stmtCount;
        }
        return -1;
    }
}