package com.puzzlang.analysis;

import com.puzzlang.ast.Nodes.*;
import com.puzzlang.ast.SourceLocation;
import com.puzzlang.diagnostic.DiagnosticCollector;

import java.util.*;

/**
 * Semantic analysis pass for PuzzLang.
 * 
 * Currently performs:
 * - Variable scope tracking (undefined variable detection)
 * - Unused variable warnings
 * 
 * Future additions:
 * - Type checking (when types are added)
 * - Constant folding hints
 * - Dead code detection
 */
public class SemanticAnalyzer {
    
    private final DiagnosticCollector diagnostics;
    private final Deque<Scope> scopes = new ArrayDeque<>();
    
    // Error codes
    public static final String E_UNDEFINED_VAR = "E001";
    public static final String E_INVALID_ITERABLE = "E002";
    public static final String W_UNUSED_VAR = "W001";
    public static final String W_SHADOWED_VAR = "W002";
    
    public SemanticAnalyzer(DiagnosticCollector diagnostics) {
        this.diagnostics = diagnostics;
    }
    
    /**
     * Analyzes the program and reports any semantic errors/warnings.
     * Call diagnostics.throwIfErrors() after to halt on errors.
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
            case Program p -> p.stmts().forEach(this::analyzeStmt);
            
            case VarDecl vd -> {
                // Analyze the value first (before defining the variable)
                analyzeExpr(vd.value());
                
                // Check for shadowing in current scope
                if (currentScope().isDefined(vd.name())) {
                    // PuzzLang allows re-assignment via `let`, so this is just updating
                    currentScope().markUsed(vd.name());
                } else if (isDefinedInOuterScope(vd.name())) {
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
            
            case ExprStmt es -> analyzeExpr(es.expr());
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
            
            case RangeLit r -> {
                analyzeExpr(r.start());
                analyzeExpr(r.end());
            }
            
            case RangeInclusive r -> {
                analyzeExpr(r.start());
                analyzeExpr(r.end());
            }
            
            case MethodCall mc -> {
                analyzeExpr(mc.receiver());
                for (Expr arg : mc.args()) {
                    analyzeExpr(arg);
                }
            }
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
        var iter = scopes.iterator();
        if (iter.hasNext()) iter.next(); // Skip current scope
        while (iter.hasNext()) {
            if (iter.next().isDefined(name)) return true;
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
        for (var entry : scope.getUnused().entrySet()) {
            diagnostics.warning(entry.getValue(), W_UNUSED_VAR,
                "Variable '%s' is declared but never used", entry.getKey());
        }
    }
    
    // ── Inner class: Scope ──────────────────────────────────────────────────
    
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
        
        Map<String, SourceLocation> getUnused() {
            Map<String, SourceLocation> unused = new HashMap<>();
            for (var entry : variables.entrySet()) {
                if (!used.contains(entry.getKey())) {
                    unused.put(entry.getKey(), entry.getValue());
                }
            }
            return unused;
        }
        
        @Override
        public String toString() {
            return "Scope[" + name + "]";
        }
    }
}
