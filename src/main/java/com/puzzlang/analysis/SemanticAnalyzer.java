package com.puzzlang.analysis;

import com.puzzlang.ast.*;
import static com.puzzlang.ast.Nodes.*;
import com.puzzlang.diagnostic.*;
import com.puzzlang.runtime.builtins.MathBuiltins;

import java.util.*;

/**
 * SemanticAnalyzer — performs semantic analysis on the AST.
 *
 * Checks:
 *   - Variable definitions before use
 *   - Unused variable warnings
 *   - Variable shadowing warnings
 *   - Unknown function warnings
 *   - Destructuring targets (tuple/list sizes)
 *   - List comprehension variable scoping
 */
public class SemanticAnalyzer {

    // Error/warning codes
    private static final String E_UNDEFINED_VAR = "E001";
    private static final String W_UNUSED_VAR = "W001";
    private static final String W_SHADOWED_VAR = "W002";
    private static final String W_UNKNOWN_FUNCTION = "W003";

    private final DiagnosticCollector diagnostics;
    private final Deque<Scope> scopes = new ArrayDeque<>();

    public SemanticAnalyzer(DiagnosticCollector diagnostics) {
        this.diagnostics = diagnostics;
    }

    /**
     * Analyzes the given program AST.
     */
    public void analyze(Program program) {
        pushScope("global");
        for (Stmt stmt : program.stmts()) {
            analyzeStmt(stmt);
        }
        checkUnusedVariables();
        popScope();
    }

    // ── Statement analysis ──────────────────────────────────────────────────

    private void analyzeStmt(Stmt stmt) {
        switch (stmt) {
            case VarDecl vd -> {
                // First analyze the value expression
                analyzeExpr(vd.value());
                
                // Then define the target variable(s)
                defineTarget(vd.target());
            }

            case PrintStmt ps -> analyzeExpr(ps.value());

            case IfStmt is -> {
                analyzeExpr(is.condition());
                pushScope("if-then");
                for (Stmt s : is.body()) analyzeStmt(s);
                checkUnusedVariables();
                popScope();
                
                if (!is.elseBody().isEmpty()) {
                    pushScope("if-else");
                    for (Stmt s : is.elseBody()) analyzeStmt(s);
                    checkUnusedVariables();
                    popScope();
                }
            }

            case WhileStmt ws -> {
                analyzeExpr(ws.condition());
                pushScope("while");
                for (Stmt s : ws.body()) analyzeStmt(s);
                checkUnusedVariables();
                popScope();
            }

            case ForIn fi -> {
                analyzeExpr(fi.iterable());
                pushScope("for-in");
                
                // Define the loop variable(s)
                defineTarget(fi.target());
                markTargetUsed(fi.target());  // Mark as used since iteration uses it

                for (Stmt s : fi.body()) analyzeStmt(s);
                checkUnusedVariables();
                popScope();
            }

            case MatchStmt ms -> {
                analyzeExpr(ms.subject());

                for (MatchArm arm : ms.arms()) {
                    pushScope("match-arm");

                    // Define captured variables based on pattern type
                    switch (arm.pattern()) {
                        case StringPattern sp -> {
                            for (String capture : sp.captureNames()) {
                                if (isDefinedInOuterScope(capture)) {
                                    diagnostics.warning(arm.location(), W_SHADOWED_VAR,
                                            "Capture '%s' shadows outer variable", capture);
                                }
                                define(capture, arm.location());
                                currentScope().markUsed(capture);
                            }
                        }
                        case TuplePattern tp -> {
                            for (String capture : tp.captureNames()) {
                                if (isDefinedInOuterScope(capture)) {
                                    diagnostics.warning(arm.location(), W_SHADOWED_VAR,
                                            "Capture '%s' shadows outer variable", capture);
                                }
                                define(capture, arm.location());
                                currentScope().markUsed(capture);
                            }
                        }
                        case WildcardPattern wp -> {
                            // No captures
                        }
                    }

                    analyzeStmt(arm.body());
                    checkUnusedVariables();
                    popScope();
                }
            }

            case ExprStmt es -> analyzeExpr(es.expr());

            case Program p -> p.stmts().forEach(this::analyzeStmt);
        }
    }

    // ── Expression analysis ─────────────────────────────────────────────────

    private void analyzeExpr(Expr expr) {
        switch (expr) {
            case IntLit ignored -> {}
            case StringLit ignored -> {}
            case BoolLit ignored -> {}

            case VarRef vr -> {
                if (!isDefined(vr.name())) {
                    diagnostics.error(vr.location(), E_UNDEFINED_VAR,
                            "Undefined variable '%s'", vr.name());
                } else {
                    markUsed(vr.name());
                }
            }

            case BinOp bo -> {
                analyzeExpr(bo.left());
                analyzeExpr(bo.right());
            }

            case RangeLit rl -> {
                analyzeExpr(rl.start());
                analyzeExpr(rl.end());
            }

            case RangeInclusive ri -> {
                analyzeExpr(ri.start());
                analyzeExpr(ri.end());
            }

            case MethodCall mc -> {
                analyzeExpr(mc.receiver());
                for (Expr arg : mc.args()) {
                    analyzeExpr(arg);
                }
            }

            case FunctionCall fc -> {
                // Check if function exists
                if (!MathBuiltins.hasFunction(fc.name())) {
                    diagnostics.warning(fc.location(), W_UNKNOWN_FUNCTION,
                            "Unknown function '%s'", fc.name());
                }
                for (Expr arg : fc.args()) {
                    analyzeExpr(arg);
                }
            }

            case StdinCall ignored -> {}

            case ReadCall rc -> analyzeExpr(rc.path());

            case TupleLit tl -> {
                for (Expr elem : tl.elements()) {
                    analyzeExpr(elem);
                }
            }

            case ListLit ll -> {
                for (Expr elem : ll.elements()) {
                    analyzeExpr(elem);
                }
            }

            case ListComprehension lc -> {
                // Analyze iterable in outer scope
                analyzeExpr(lc.iterable());
                
                // Create scope for comprehension
                pushScope("list-comprehension");
                
                // Define target variable(s)
                defineTarget(lc.target());
                markTargetUsed(lc.target());
                
                // For nested comprehensions
                if (lc.isNested()) {
                    analyzeExpr(lc.secondIterable());
                    defineTarget(lc.secondTarget());
                    markTargetUsed(lc.secondTarget());
                }
                
                // Analyze condition if present
                if (lc.hasCondition()) {
                    analyzeExpr(lc.condition());
                }
                
                // Analyze element expression
                analyzeExpr(lc.element());
                
                checkUnusedVariables();
                popScope();
            }
        }
    }

    // ── Destructure target helpers ──────────────────────────────────────────

    /**
     * Define variables from a destructure target.
     */
    private void defineTarget(DestructureTarget target) {
        switch (target) {
            case SingleTarget st -> {
                if (isDefinedInOuterScope(st.name())) {
                    // Allow shadowing in let - common pattern in PuzzLang
                }
                define(st.name(), st.location());
            }
            case TupleTarget tt -> {
                for (String name : tt.names()) {
                    if (isDefinedInOuterScope(name)) {
                        // Allow shadowing
                    }
                    define(name, tt.location());
                }
            }
        }
    }

    /**
     * Mark all variables in a target as used.
     */
    private void markTargetUsed(DestructureTarget target) {
        for (String name : target.names()) {
            currentScope().markUsed(name);
        }
    }

    // ── Scope management ────────────────────────────────────────────────────

    private void pushScope(String name) {
        scopes.push(new Scope(name));
    }

    private void popScope() {
        scopes.pop();
    }

    private Scope currentScope() {
        return scopes.peek();
    }

    private void define(String name, SourceLocation location) {
        currentScope().define(name, location);
    }

    private boolean isDefined(String name) {
        for (Scope scope : scopes) {
            if (scope.isDefined(name)) return true;
        }
        return false;
    }

    private boolean isDefinedInOuterScope(String name) {
        boolean first = true;
        for (Scope scope : scopes) {
            if (first) {
                first = false;
                continue;  // Skip current scope
            }
            if (scope.isDefined(name)) return true;
        }
        return false;
    }

    private void markUsed(String name) {
        for (Scope scope : scopes) {
            if (scope.isDefined(name)) {
                scope.markUsed(name);
                return;
            }
        }
    }

    private void checkUnusedVariables() {
        Scope scope = currentScope();
        for (String unused : scope.getUnusedVariables()) {
            // Don't warn about loop variables or captures
            diagnostics.warning(scope.getLocation(unused), W_UNUSED_VAR,
                    "Variable '%s' is defined but never used", unused);
        }
    }

    // ── Inner: Scope ────────────────────────────────────────────────────────

    private static class Scope {
        private final String name;
        private final Map<String, SourceLocation> variables = new HashMap<>();
        private final Set<String> used = new HashSet<>();

        Scope(String name) {
            this.name = name;
        }

        void define(String varName, SourceLocation location) {
            variables.put(varName, location);
        }

        boolean isDefined(String varName) {
            return variables.containsKey(varName);
        }

        void markUsed(String varName) {
            used.add(varName);
        }

        SourceLocation getLocation(String varName) {
            return variables.get(varName);
        }

        List<String> getUnusedVariables() {
            List<String> unused = new ArrayList<>();
            for (String var : variables.keySet()) {
                if (!used.contains(var)) {
                    unused.add(var);
                }
            }
            return unused;
        }
    }
}
