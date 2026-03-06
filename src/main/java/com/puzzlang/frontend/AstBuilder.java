package com.puzzlang.frontend;

import com.puzzlang.ast.*;
import static com.puzzlang.ast.Nodes.*;
import com.puzzlang.parser.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.util.*;
import java.util.regex.*;

/**
 * AstBuilder — ANTLR parse tree → typed AST.
 *
 * Transforms the CST from ANTLR into a clean AST defined in Nodes.java.
 * Handles all expression types including the new FunctionCall.
 */
public class AstBuilder extends PuzzLangBaseVisitor<Object> {

    // Regex to find {captureName} placeholders in match patterns
    private static final Pattern CAPTURE_PATTERN = Pattern.compile("\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}");

    // ── Program ─────────────────────────────────────────────────────────────

    @Override
    public Program visitProgram(PuzzLangParser.ProgramContext ctx) {
        List<Stmt> stmts = new ArrayList<>();
        for (PuzzLangParser.StatementContext sc : ctx.statement()) {
            stmts.add(visitStatement(sc));
        }
        return new Program(stmts);
    }

    // ── Statements ──────────────────────────────────────────────────────────

    public Stmt visitStatement(PuzzLangParser.StatementContext ctx) {
        if (ctx.varDecl()    != null) return visitVarDecl(ctx.varDecl());
        if (ctx.printStmt()  != null) return visitPrintStmt(ctx.printStmt());
        if (ctx.ifStmt()     != null) return visitIfStmt(ctx.ifStmt());
        if (ctx.whileStmt()  != null) return visitWhileStmt(ctx.whileStmt());
        if (ctx.forInStmt()  != null) return visitForInStmt(ctx.forInStmt());
        if (ctx.matchStmt()  != null) return visitMatchStmt(ctx.matchStmt());
        if (ctx.exprStmt()   != null) return visitExprStmt(ctx.exprStmt());
        throw new RuntimeException("Unknown statement: " + ctx.getText());
    }

    @Override
    public VarDecl visitVarDecl(PuzzLangParser.VarDeclContext ctx) {
        String name = ctx.ID().getText();
        Expr value = visitExprCtx(ctx.expr());
        return new VarDecl(name, value, SourceLocation.fromContext(ctx));
    }

    @Override
    public PrintStmt visitPrintStmt(PuzzLangParser.PrintStmtContext ctx) {
        Expr value = visitExprCtx(ctx.expr());
        return new PrintStmt(value, SourceLocation.fromContext(ctx));
    }

    @Override
    public IfStmt visitIfStmt(PuzzLangParser.IfStmtContext ctx) {
        Expr cond = visitExprCtx(ctx.expr());

        List<PuzzLangParser.StatementContext> allStmts = ctx.statement();
        int elseIdx = findElseIndex(ctx);

        List<Stmt> body;
        List<Stmt> elseBody;
        if (elseIdx < 0) {
            body = mapStmts(allStmts, 0, allStmts.size());
            elseBody = List.of();
        } else {
            body = mapStmts(allStmts, 0, elseIdx);
            elseBody = mapStmts(allStmts, elseIdx, allStmts.size());
        }
        return new IfStmt(cond, body, elseBody, SourceLocation.fromContext(ctx));
    }

    @Override
    public WhileStmt visitWhileStmt(PuzzLangParser.WhileStmtContext ctx) {
        Expr cond = visitExprCtx(ctx.expr());
        List<Stmt> body = new ArrayList<>();
        for (PuzzLangParser.StatementContext sc : ctx.statement()) {
            body.add(visitStatement(sc));
        }
        return new WhileStmt(cond, body, SourceLocation.fromContext(ctx));
    }

    @Override
    public ForIn visitForInStmt(PuzzLangParser.ForInStmtContext ctx) {
        String var = ctx.ID().getText();
        Expr iterable = visitExprCtx(ctx.expr());
        List<Stmt> body = new ArrayList<>();
        for (PuzzLangParser.StatementContext sc : ctx.statement()) {
            body.add(visitStatement(sc));
        }
        return new ForIn(var, iterable, body, SourceLocation.fromContext(ctx));
    }

    // ── Match Statement ─────────────────────────────────────────────────────

    @Override
    public MatchStmt visitMatchStmt(PuzzLangParser.MatchStmtContext ctx) {
        Expr subject = visitExprCtx(ctx.expr());
        List<MatchArm> arms = new ArrayList<>();
        for (PuzzLangParser.MatchArmContext armCtx : ctx.matchArm()) {
            arms.add(visitMatchArm(armCtx));
        }
        return new MatchStmt(subject, arms, SourceLocation.fromContext(ctx));
    }

    @Override
    public MatchArm visitMatchArm(PuzzLangParser.MatchArmContext ctx) {
        MatchPattern pattern = visitMatchPattern(ctx.matchPattern());
        Stmt body = visitStatement(ctx.statement());
        return new MatchArm(pattern, body, SourceLocation.fromContext(ctx));
    }

    public MatchPattern visitMatchPattern(PuzzLangParser.MatchPatternContext ctx) {
        SourceLocation loc = SourceLocation.fromContext(ctx);

        if (ctx instanceof PuzzLangParser.PatternStringContext ps) {
            String raw = ps.STRING().getText();
            String template = raw.substring(1, raw.length() - 1);
            List<String> captureNames = extractCaptureNames(template);
            return new StringPattern(template, captureNames, loc);
        }

        if (ctx instanceof PuzzLangParser.PatternWildcardContext) {
            return new WildcardPattern(loc);
        }

        throw new RuntimeException("Unknown match pattern: " + ctx.getClass().getSimpleName());
    }

    /**
     * Extracts capture variable names from a pattern template.
     * "move {n} from {a} to {b}" → ["n", "a", "b"]
     */
    private List<String> extractCaptureNames(String template) {
        List<String> names = new ArrayList<>();
        Matcher matcher = CAPTURE_PATTERN.matcher(template);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    // ── Expression Statement ────────────────────────────────────────────────

    @Override
    public ExprStmt visitExprStmt(PuzzLangParser.ExprStmtContext ctx) {
        Expr expr = visitExprCtx(ctx.expr());
        return new ExprStmt(expr, SourceLocation.fromContext(ctx));
    }

    public Expr visitExprCtx(PuzzLangParser.ExprContext ctx) {
        SourceLocation loc = SourceLocation.fromContext(ctx);

        if (ctx instanceof PuzzLangParser.IntLitContext c)
            return new IntLit(Integer.parseInt(c.INT().getText()), loc);

        if (ctx instanceof PuzzLangParser.StringLitContext c) {
            String raw = c.STRING().getText();
            String content = raw.substring(1, raw.length() - 1);
            String unescaped = unescapeString(content);
            return new StringLit(unescaped, loc);
        }

        if (ctx instanceof PuzzLangParser.BoolLitContext c)
            return new BoolLit(c.BOOL().getText().equals("true"), loc);

        if (ctx instanceof PuzzLangParser.VarContext c)
            return new VarRef(c.ID().getText(), loc);

        if (ctx instanceof PuzzLangParser.ParensContext c)
            return visitExprCtx(c.expr());

        // ── Binary operators ─────────────────────────────────────────────
        if (ctx instanceof PuzzLangParser.MulDivModContext c) {
            Expr left = visitExprCtx(c.expr(0));
            Expr right = visitExprCtx(c.expr(1));
            return new BinOp(left, c.op.getText(), right, loc);
        }

        if (ctx instanceof PuzzLangParser.AddSubContext c) {
            Expr left = visitExprCtx(c.expr(0));
            Expr right = visitExprCtx(c.expr(1));
            return new BinOp(left, c.op.getText(), right, loc);
        }

        if (ctx instanceof PuzzLangParser.CompareContext c) {
            Expr left = visitExprCtx(c.expr(0));
            Expr right = visitExprCtx(c.expr(1));
            return new BinOp(left, c.op.getText(), right, loc);
        }

        // ── Range expressions ────────────────────────────────────────────
        if (ctx instanceof PuzzLangParser.RangeLitContext c)
            return new RangeLit(visitExprCtx(c.expr(0)), visitExprCtx(c.expr(1)), loc);

        if (ctx instanceof PuzzLangParser.RangeInclusiveContext c)
            return new RangeInclusive(visitExprCtx(c.expr(0)), visitExprCtx(c.expr(1)), loc);

        // ── Method call: receiver.method(args) ───────────────────────────
        if (ctx instanceof PuzzLangParser.MethodCallContext c) {
            Expr receiver = visitExprCtx(c.expr(0));
            String method = c.method.getText();
            // args: expr(1)..expr(n)  (expr(0) is the receiver)
            List<Expr> args = new ArrayList<>();
            for (int i = 1; i < c.expr().size(); i++) {
                args.add(visitExprCtx(c.expr(i)));
            }
            return new MethodCall(receiver, method, args, loc);
        }

        // ── Global function call: name(args) ─────────────────────────────
        if (ctx instanceof PuzzLangParser.FunctionCallContext c) {
            String name = c.ID().getText();
            List<Expr> args = new ArrayList<>();
            for (PuzzLangParser.ExprContext exprCtx : c.expr()) {
                args.add(visitExprCtx(exprCtx));
            }
            return new FunctionCall(name, args, loc);
        }

        // ── I/O built-ins ────────────────────────────────────────────────
        if (ctx instanceof PuzzLangParser.StdinCallContext)
            return new StdinCall(loc);

        if (ctx instanceof PuzzLangParser.ReadCallContext c)
            return new ReadCall(visitExprCtx(c.expr()), loc);

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

    /**
     * Process escape sequences in string literals.
     * Converts \n, \t, \r, \\, \" to their actual characters.
     */
    private String unescapeString(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                switch (next) {
                    case 'n' -> { sb.append('\n'); i++; }
                    case 't' -> { sb.append('\t'); i++; }
                    case 'r' -> { sb.append('\r'); i++; }
                    case '\\' -> { sb.append('\\'); i++; }
                    case '"' -> { sb.append('"'); i++; }
                    default -> sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
