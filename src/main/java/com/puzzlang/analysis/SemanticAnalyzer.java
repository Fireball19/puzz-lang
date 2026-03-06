package com.puzzlang.analysis;

import com.puzzlang.ast.*;
import static com.puzzlang.ast.Nodes.*;
import com.puzzlang.diagnostic.*;
import com.puzzlang.runtime.builtins.MathBuiltins;

import java.util.*;

/**
 * SemanticAnalyzer — performs semantic analysis on the AST.
 *
 * Currently checks:
 *   - Variable definitions before use
 *   - Unused variable warnings
 *   - Variable shadowing warnings
 *   - Unknown function warnings
 *
 * Could be extended to check:
 *   - Type compatibility (when types are added)
 *   - Function arity
 *   - Unreachable code
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
                analyzeExpr(vd.value());
                // Check if we're shadowing an outer variable
                if (isDefinedInOuterScope(vd.name())) {
                    diagnostics.warning(vd.location(), W_SHADOWED_VAR,
                            "Variable '%s' shadows outer variable", vd.name());
                }
                define(vd.name(), vd.location());
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
                // Analyze iterable before creating loop scope
                analyzeExpr(fi.iterable());

                pushScope("for-in");
                // Loop variable is defined in loop scope
                define(fi.var(), fi.location());
                // Mark it as used since iteration uses it implicitly
                currentScope().markUsed(fi.var());

                for (Stmt s : fi.body()) analyzeStmt(s);
                checkUnusedVariables();
                popScope();
            }

            case MatchStmt ms -> {
                // Analyze the subject expression
                analyzeExpr(ms.subject());

                for (MatchArm arm : ms.arms()) {
                    pushScope("match-arm");

                    // Define captured variables in this arm's scope
                    if (arm.pattern() instanceof StringPattern sp) {
                        for (String capture : sp.captureNames()) {
                            // Check if we're shadowing an outer variable
                            if (isDefinedInOuterScope(capture)) {
                                diagnostics.warning(arm.location(), W_SHADOWED_VAR,
                                        "Capture '%s' shadows outer variable", capture);
                            }
                            define(capture, arm.location());
                            // Mark as used since pattern matching uses it implicitly
                            currentScope().markUsed(capture);
                        }
                    }
                    // WildcardPattern has no captures

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

            case VarRef ref -> {
                if (!isDefined(ref.name())) {
                    diagnostics.error(ref.location(), E_UNDEFINED_VAR,
                            "Undefined variable '%s'", ref.name());
                } else {
                    markUsed(ref.name());
                }
            }

            case BinOp op -> {
                analyzeExpr(op.left());
                analyzeExpr(op.right());
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
                // Analyze all arguments
                for (Expr arg : fc.args()) {
                    analyzeExpr(arg);
                }
            }

            case StdinCall ignored -> {}

            case ReadCall rc -> analyzeExpr(rc.path());
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
                continue; // Skip current scope
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
        for (String name : scope.getUnused()) {
            diagnostics.warning(scope.getLocation(name), W_UNUSED_VAR,
                    "Variable '%s' is never used", name);
        }
    }

    // ── Inner class: Scope ──────────────────────────────────────────────────

    private static class Scope {
        private final String name;
        private final Map<String, SourceLocation> definitions = new HashMap<>();
        private final Set<String> used = new HashSet<>();

        Scope(String name) {
            this.name = name;
        }

        void define(String varName, SourceLocation location) {
            definitions.put(varName, location);
        }

        boolean isDefined(String varName) {
            return definitions.containsKey(varName);
        }

        void markUsed(String varName) {
            used.add(varName);
        }

        SourceLocation getLocation(String varName) {
            return definitions.get(varName);
        }

        Set<String> getUnused() {
            Set<String> unused = new HashSet<>(definitions.keySet());
            unused.removeAll(used);
            return unused;
        }
    }
}
