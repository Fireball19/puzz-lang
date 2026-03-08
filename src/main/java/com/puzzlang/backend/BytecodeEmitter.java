package com.puzzlang.backend;

import com.puzzlang.ast.Nodes.*;
import org.objectweb.asm.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

/**
 * BytecodeEmitter — AST → JVM bytecode via ASM.
 *
 * All values are Object (dynamic typing).
 *
 * Emits:
 *   - Range construction: INVOKESTATIC PuzzRuntime.makeRange/makeRangeInclusive
 *   - Tuple construction: INVOKESTATIC PuzzRuntime.makeTuple
 *   - List construction: INVOKESTATIC PuzzRuntime.makeList
 *   - List comprehensions: loop + conditional + list building
 *   - Method calls: INVOKESTATIC PuzzRuntime.callMethod
 *   - Function calls: INVOKESTATIC PuzzRuntime.callFunction
 *   - For-in loops: get iterator, loop with hasNext/next
 *   - Destructuring: tuple unpacking in let/for
 *   - I/O calls: INVOKESTATIC PuzzIO.stdin/readFile
 *   - Match statements: pattern matching with captures (strings and tuples)
 */
public class BytecodeEmitter {

    private static final String RUNTIME = "com/puzzlang/runtime/PuzzRuntime";
    private static final String IO      = "com/puzzlang/runtime/PuzzIO";
    private static final String MATCH   = "com/puzzlang/runtime/PuzzMatch";
    private static final String MATCH_RESULT = "com/puzzlang/runtime/PuzzMatch$MatchResult";
    private static final String TUPLE   = "com/puzzlang/runtime/PuzzTuple";
    private static final String LIST    = "com/puzzlang/runtime/PuzzList";

    private final String className;
    private ClassWriter  cw;
    private MethodVisitor mv;
    private final Map<String, Integer> locals  = new HashMap<>();
    private int nextSlot = 1;  // slot 0 = String[] args

    public BytecodeEmitter(String className) {
        this.className = className;
    }

    public byte[] emit(Program program) {
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(V17, ACC_PUBLIC | ACC_SUPER, className, null, "java/lang/Object", null);
        emitDefaultConstructor();

        mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "main",
                "([Ljava/lang/String;)V", null, null);
        mv.visitCode();

        for (Stmt stmt : program.stmts()) emitStmt(stmt);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    // ── Statements ───────────────────────────────────────────────────────────

    private void emitStmt(Stmt stmt) {
        switch (stmt) {
            case VarDecl vd   -> emitVarDecl(vd);
            case PrintStmt ps -> emitPrint(ps);
            case IfStmt is    -> emitIf(is);
            case WhileStmt ws -> emitWhile(ws);
            case ForIn fi     -> emitForIn(fi);
            case MatchStmt ms -> emitMatch(ms);
            case ExprStmt es  -> { emitExpr(es.expr()); mv.visitInsn(POP); }
            case Program p    -> p.stmts().forEach(this::emitStmt);
        }
    }

    private void emitVarDecl(VarDecl vd) {
        emitExpr(vd.value());
        emitDestructuringAssignment(vd.target());
    }

    /**
     * Emit code to destructure a value on the stack into variables.
     * Stack: value → (empty)
     */
    private void emitDestructuringAssignment(DestructureTarget target) {
        switch (target) {
            case SingleTarget st -> {
                // Simple case: store directly
                mv.visitVarInsn(ASTORE, getOrCreateSlot(st.name()));
            }
            case TupleTarget tt -> {
                // Tuple destructuring: value must be a tuple
                // Call tuple.unpack(n) to get Object[] array
                int tempSlot = nextSlot++;
                mv.visitVarInsn(ASTORE, tempSlot);  // Save value
                
                // Cast to PuzzTuple and call unpack
                mv.visitVarInsn(ALOAD, tempSlot);
                mv.visitLdcInsn(tt.size());
                mv.visitMethodInsn(INVOKESTATIC, RUNTIME, "unpackTuple",
                        "(Ljava/lang/Object;I)[Ljava/lang/Object;", false);
                
                // Store array in temp
                int arraySlot = nextSlot++;
                mv.visitVarInsn(ASTORE, arraySlot);
                
                // Extract each element
                List<String> names = tt.names();
                for (int i = 0; i < names.size(); i++) {
                    mv.visitVarInsn(ALOAD, arraySlot);
                    mv.visitLdcInsn(i);
                    mv.visitInsn(AALOAD);
                    mv.visitVarInsn(ASTORE, getOrCreateSlot(names.get(i)));
                }
            }
        }
    }

    private void emitPrint(PrintStmt ps) {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        emitExpr(ps.value());
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream",
                "println", "(Ljava/lang/Object;)V", false);
    }

    private void emitIf(IfStmt is) {
        Label elseLabel = new Label();
        Label endLabel  = new Label();
        emitExpr(is.condition());
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME, "isTruthy",
                "(Ljava/lang/Object;)Z", false);
        mv.visitJumpInsn(IFEQ, elseLabel);
        for (Stmt s : is.body())     emitStmt(s);
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(elseLabel);
        for (Stmt s : is.elseBody()) emitStmt(s);
        mv.visitLabel(endLabel);
    }

    private void emitWhile(WhileStmt ws) {
        Label loopStart = new Label();
        Label loopEnd   = new Label();
        mv.visitLabel(loopStart);
        emitExpr(ws.condition());
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME, "isTruthy",
                "(Ljava/lang/Object;)Z", false);
        mv.visitJumpInsn(IFEQ, loopEnd);
        for (Stmt s : ws.body()) emitStmt(s);
        mv.visitJumpInsn(GOTO, loopStart);
        mv.visitLabel(loopEnd);
    }

    /**
     * for TARGET in ITERABLE: BODY
     *
     * TARGET can be:
     *   - SingleTarget: for x in items
     *   - TupleTarget: for (x, y) in pairs
     */
    private void emitForIn(ForIn fi) {
        int iterSlot = nextSlot++;

        // Get iterator
        emitExpr(fi.iterable());
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME, "toIterable",
                "(Ljava/lang/Object;)Ljava/lang/Iterable;", false);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/Iterable", "iterator",
                "()Ljava/util/Iterator;", true);
        mv.visitVarInsn(ASTORE, iterSlot);

        // Loop
        Label loopStart = new Label();
        Label loopEnd   = new Label();

        mv.visitLabel(loopStart);

        // iter.hasNext()
        mv.visitVarInsn(ALOAD, iterSlot);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext",
                "()Z", true);
        mv.visitJumpInsn(IFEQ, loopEnd);

        // Get next value
        mv.visitVarInsn(ALOAD, iterSlot);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next",
                "()Ljava/lang/Object;", true);

        // Destructure into target variable(s)
        emitDestructuringAssignment(fi.target());

        // Body
        for (Stmt s : fi.body()) emitStmt(s);

        mv.visitJumpInsn(GOTO, loopStart);
        mv.visitLabel(loopEnd);
    }

    /**
     * match SUBJECT:
     *     "pattern {x}" -> stmt
     *     (0, 0) -> stmt
     *     (x, y) -> stmt
     *     _ -> stmt
     */
    private void emitMatch(MatchStmt ms) {
        Label endLabel = new Label();

        // Evaluate subject once, store in temp slot
        emitExpr(ms.subject());
        int subjectSlot = nextSlot++;
        mv.visitVarInsn(ASTORE, subjectSlot);

        for (MatchArm arm : ms.arms()) {
            Label nextArmLabel = new Label();

            switch (arm.pattern()) {
                case WildcardPattern wp -> {
                    // Wildcard always matches, just emit body
                    emitStmt(arm.body());
                    mv.visitJumpInsn(GOTO, endLabel);
                }
                
                case StringPattern sp -> {
                    emitStringPatternMatch(subjectSlot, sp, arm, nextArmLabel, endLabel);
                }
                
                case TuplePattern tp -> {
                    emitTuplePatternMatch(subjectSlot, tp, arm, nextArmLabel, endLabel);
                }
            }

            mv.visitLabel(nextArmLabel);
        }

        mv.visitLabel(endLabel);
    }

    /**
     * Emit code for string pattern matching.
     */
    private void emitStringPatternMatch(int subjectSlot, StringPattern sp, MatchArm arm,
                                        Label nextArmLabel, Label endLabel) {
        // Convert subject to string
        mv.visitVarInsn(ALOAD, subjectSlot);
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME, "toString",
                "(Ljava/lang/Object;)Ljava/lang/String;", false);
        int stringSlot = nextSlot++;
        mv.visitVarInsn(ASTORE, stringSlot);

        // Call PuzzMatch.tryMatch(subject, template, captureNames)
        mv.visitVarInsn(ALOAD, stringSlot);
        mv.visitLdcInsn(sp.template());
        emitCaptureNamesList(sp.captureNames());

        mv.visitMethodInsn(INVOKESTATIC, MATCH, "tryMatch",
                "(Ljava/lang/String;Ljava/lang/String;Ljava/util/List;)" +
                "L" + MATCH_RESULT + ";", false);

        int resultSlot = nextSlot++;
        mv.visitVarInsn(ASTORE, resultSlot);

        // Check if matched
        mv.visitVarInsn(ALOAD, resultSlot);
        mv.visitMethodInsn(INVOKEVIRTUAL, MATCH_RESULT, "matched", "()Z", false);
        mv.visitJumpInsn(IFEQ, nextArmLabel);

        // Extract captures
        for (String captureName : sp.captureNames()) {
            mv.visitVarInsn(ALOAD, resultSlot);
            mv.visitMethodInsn(INVOKEVIRTUAL, MATCH_RESULT, "captures",
                    "()Ljava/util/Map;", false);
            mv.visitLdcInsn(captureName);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get",
                    "(Ljava/lang/Object;)Ljava/lang/Object;", true);
            mv.visitVarInsn(ASTORE, getOrCreateSlot(captureName));
        }

        emitStmt(arm.body());
        mv.visitJumpInsn(GOTO, endLabel);
    }

    /**
     * Emit code for tuple pattern matching.
     * Pattern: (0, 0), (x, 0), (x, y), (_, y)
     */
    private void emitTuplePatternMatch(int subjectSlot, TuplePattern tp, MatchArm arm,
                                       Label nextArmLabel, Label endLabel) {
        // Check if subject is a tuple with correct size
        mv.visitVarInsn(ALOAD, subjectSlot);
        mv.visitLdcInsn(tp.size());
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME, "isTupleOfSize",
                "(Ljava/lang/Object;I)Z", false);
        mv.visitJumpInsn(IFEQ, nextArmLabel);

        // Get elements array
        mv.visitVarInsn(ALOAD, subjectSlot);
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME, "getTupleElements",
                "(Ljava/lang/Object;)[Ljava/lang/Object;", false);
        int elementsSlot = nextSlot++;
        mv.visitVarInsn(ASTORE, elementsSlot);

        // Check each element and bind variables
        List<TuplePatternElement> elements = tp.elements();
        for (int i = 0; i < elements.size(); i++) {
            TuplePatternElement elem = elements.get(i);
            
            switch (elem) {
                case LiteralElement le -> {
                    // Check if element equals literal
                    mv.visitVarInsn(ALOAD, elementsSlot);
                    mv.visitLdcInsn(i);
                    mv.visitInsn(AALOAD);
                    emitLiteralValue(le.value());
                    mv.visitMethodInsn(INVOKESTATIC, RUNTIME, "equals",
                            "(Ljava/lang/Object;Ljava/lang/Object;)Z", false);
                    mv.visitJumpInsn(IFEQ, nextArmLabel);
                }
                
                case BindingElement be -> {
                    // Bind element to variable
                    mv.visitVarInsn(ALOAD, elementsSlot);
                    mv.visitLdcInsn(i);
                    mv.visitInsn(AALOAD);
                    mv.visitVarInsn(ASTORE, getOrCreateSlot(be.name()));
                }
                
                case WildcardElement we -> {
                    // Do nothing - wildcard matches anything
                }
            }
        }

        emitStmt(arm.body());
        mv.visitJumpInsn(GOTO, endLabel);
    }

    /**
     * Emit a literal value (for tuple pattern matching).
     */
    private void emitLiteralValue(Object value) {
        if (value instanceof Integer i) {
            mv.visitLdcInsn(i);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf",
                    "(I)Ljava/lang/Integer;", false);
        } else if (value instanceof String s) {
            mv.visitLdcInsn(s);
        } else if (value instanceof Boolean b) {
            mv.visitLdcInsn(b ? 1 : 0);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf",
                    "(Z)Ljava/lang/Boolean;", false);
        } else {
            throw new RuntimeException("Unknown literal type: " + value.getClass());
        }
    }

    /**
     * Emits code to create a List<String> containing the capture names.
     */
    private void emitCaptureNamesList(List<String> captureNames) {
        mv.visitLdcInsn(captureNames.size());
        mv.visitTypeInsn(ANEWARRAY, "java/lang/String");

        for (int i = 0; i < captureNames.size(); i++) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn(i);
            mv.visitLdcInsn(captureNames.get(i));
            mv.visitInsn(AASTORE);
        }

        mv.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "asList",
                "([Ljava/lang/Object;)Ljava/util/List;", false);
    }

    // ── Expressions ──────────────────────────────────────────────────────────

    private void emitExpr(Expr expr) {
        switch (expr) {
            case IntLit lit -> {
                mv.visitLdcInsn(lit.value());
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf",
                        "(I)Ljava/lang/Integer;", false);
            }
            case StringLit lit -> mv.visitLdcInsn(lit.value());
            case BoolLit lit -> {
                mv.visitLdcInsn(lit.value() ? 1 : 0);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf",
                        "(Z)Ljava/lang/Boolean;", false);
            }
            case VarRef ref ->
                    mv.visitVarInsn(ALOAD, getOrCreateSlot(ref.name()));

            case BinOp op   -> emitBinOp(op);

            // ── Ranges ────────────────────────────────────────────────────
            case RangeLit r -> {
                emitExpr(r.start());
                emitExpr(r.end());
                mv.visitMethodInsn(INVOKESTATIC, RUNTIME, "makeRange",
                        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                        false);
            }
            case RangeInclusive r -> {
                emitExpr(r.start());
                emitExpr(r.end());
                mv.visitMethodInsn(INVOKESTATIC, RUNTIME, "makeRangeInclusive",
                        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                        false);
            }

            // ── Tuple literal ─────────────────────────────────────────────
            case TupleLit t -> emitTupleLit(t);

            // ── List literal ──────────────────────────────────────────────
            case ListLit l -> emitListLit(l);

            // ── List comprehension ────────────────────────────────────────
            case ListComprehension lc -> emitListComprehension(lc);

            // ── Method calls ──────────────────────────────────────────────
            case MethodCall mc -> emitMethodCall(mc);

            // ── Function calls ────────────────────────────────────────────
            case FunctionCall fc -> emitFunctionCall(fc);

            // ── I/O built-ins ─────────────────────────────────────────────
            case StdinCall sc -> {
                mv.visitMethodInsn(INVOKESTATIC, IO, "stdin",
                        "()Ljava/lang/Object;", false);
            }
            case ReadCall rc -> {
                emitExpr(rc.path());
                mv.visitMethodInsn(INVOKESTATIC, IO, "readFile",
                        "(Ljava/lang/Object;)Ljava/lang/Object;", false);
            }
        }
    }

    /**
     * Emit tuple literal: (a, b, c)
     */
    private void emitTupleLit(TupleLit t) {
        // Create Object[] array
        mv.visitLdcInsn(t.size());
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        for (int i = 0; i < t.size(); i++) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn(i);
            emitExpr(t.elements().get(i));
            mv.visitInsn(AASTORE);
        }

        // Call PuzzRuntime.makeTuple(Object[])
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME, "makeTuple",
                "([Ljava/lang/Object;)Ljava/lang/Object;", false);
    }

    /**
     * Emit list literal: [1, 2, 3]
     */
    private void emitListLit(ListLit l) {
        if (l.isEmpty()) {
            // Empty list
            mv.visitMethodInsn(INVOKESTATIC, RUNTIME, "emptyList",
                    "()Ljava/lang/Object;", false);
        } else {
            // Create Object[] array
            mv.visitLdcInsn(l.size());
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

            for (int i = 0; i < l.size(); i++) {
                mv.visitInsn(DUP);
                mv.visitLdcInsn(i);
                emitExpr(l.elements().get(i));
                mv.visitInsn(AASTORE);
            }

            // Call PuzzRuntime.makeList(Object[])
            mv.visitMethodInsn(INVOKESTATIC, RUNTIME, "makeList",
                    "([Ljava/lang/Object;)Ljava/lang/Object;", false);
        }
    }

    /**
     * Emit list comprehension: [expr for target in iterable], [expr for target in iterable if cond]
     * 
     * Also handles nested: [(x, y) for x in xs for y in ys]
     */
    private void emitListComprehension(ListComprehension lc) {
        // Create result list
        mv.visitTypeInsn(NEW, LIST);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LIST, "<init>", "()V", false);
        int resultSlot = nextSlot++;
        mv.visitVarInsn(ASTORE, resultSlot);

        if (lc.isNested()) {
            emitNestedListComprehension(lc, resultSlot);
        } else {
            emitSimpleListComprehension(lc, resultSlot);
        }

        // Push result onto stack
        mv.visitVarInsn(ALOAD, resultSlot);
    }

    /**
     * Emit simple list comprehension (single for loop).
     */
    private void emitSimpleListComprehension(ListComprehension lc, int resultSlot) {
        int iterSlot = nextSlot++;

        // Get iterator
        emitExpr(lc.iterable());
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME, "toIterable",
                "(Ljava/lang/Object;)Ljava/lang/Iterable;", false);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/Iterable", "iterator",
                "()Ljava/util/Iterator;", true);
        mv.visitVarInsn(ASTORE, iterSlot);

        Label loopStart = new Label();
        Label loopEnd = new Label();

        mv.visitLabel(loopStart);

        // hasNext check
        mv.visitVarInsn(ALOAD, iterSlot);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
        mv.visitJumpInsn(IFEQ, loopEnd);

        // Get next and destructure
        mv.visitVarInsn(ALOAD, iterSlot);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next",
                "()Ljava/lang/Object;", true);
        emitDestructuringAssignment(lc.target());

        // Check condition if present
        Label skipAdd = null;
        if (lc.hasCondition()) {
            skipAdd = new Label();
            emitExpr(lc.condition());
            mv.visitMethodInsn(INVOKESTATIC, RUNTIME, "isTruthy",
                    "(Ljava/lang/Object;)Z", false);
            mv.visitJumpInsn(IFEQ, skipAdd);
        }

        // Add element to result
        mv.visitVarInsn(ALOAD, resultSlot);
        emitExpr(lc.element());
        mv.visitMethodInsn(INVOKEVIRTUAL, LIST, "add", "(Ljava/lang/Object;)V", false);

        if (skipAdd != null) {
            mv.visitLabel(skipAdd);
        }

        mv.visitJumpInsn(GOTO, loopStart);
        mv.visitLabel(loopEnd);
    }

    /**
     * Emit nested list comprehension (two for loops).
     */
    private void emitNestedListComprehension(ListComprehension lc, int resultSlot) {
        int iter1Slot = nextSlot++;
        int iter2Slot = nextSlot++;

        // Outer loop: for target in iterable
        emitExpr(lc.iterable());
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME, "toIterable",
                "(Ljava/lang/Object;)Ljava/lang/Iterable;", false);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/Iterable", "iterator",
                "()Ljava/util/Iterator;", true);
        mv.visitVarInsn(ASTORE, iter1Slot);

        Label outerStart = new Label();
        Label outerEnd = new Label();

        mv.visitLabel(outerStart);
        mv.visitVarInsn(ALOAD, iter1Slot);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
        mv.visitJumpInsn(IFEQ, outerEnd);

        mv.visitVarInsn(ALOAD, iter1Slot);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next",
                "()Ljava/lang/Object;", true);
        emitDestructuringAssignment(lc.target());

        // Inner loop: for secondTarget in secondIterable
        emitExpr(lc.secondIterable());
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME, "toIterable",
                "(Ljava/lang/Object;)Ljava/lang/Iterable;", false);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/Iterable", "iterator",
                "()Ljava/util/Iterator;", true);
        mv.visitVarInsn(ASTORE, iter2Slot);

        Label innerStart = new Label();
        Label innerEnd = new Label();

        mv.visitLabel(innerStart);
        mv.visitVarInsn(ALOAD, iter2Slot);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
        mv.visitJumpInsn(IFEQ, innerEnd);

        mv.visitVarInsn(ALOAD, iter2Slot);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next",
                "()Ljava/lang/Object;", true);
        emitDestructuringAssignment(lc.secondTarget());

        // Add element
        mv.visitVarInsn(ALOAD, resultSlot);
        emitExpr(lc.element());
        mv.visitMethodInsn(INVOKEVIRTUAL, LIST, "add", "(Ljava/lang/Object;)V", false);

        mv.visitJumpInsn(GOTO, innerStart);
        mv.visitLabel(innerEnd);

        mv.visitJumpInsn(GOTO, outerStart);
        mv.visitLabel(outerEnd);
    }

    /**
     * Compiles receiver.method(arg0, arg1, ...)
     */
    private void emitMethodCall(MethodCall mc) {
        emitExpr(mc.receiver());
        mv.visitLdcInsn(mc.method());

        List<Expr> args = mc.args();
        mv.visitLdcInsn(args.size());
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        for (int i = 0; i < args.size(); i++) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn(i);
            emitExpr(args.get(i));
            mv.visitInsn(AASTORE);
        }

        mv.visitMethodInsn(INVOKESTATIC, RUNTIME, "callMethod",
                "(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)" +
                "Ljava/lang/Object;",
                false);
    }

    /**
     * Compiles name(arg0, arg1, ...)
     */
    private void emitFunctionCall(FunctionCall fc) {
        mv.visitLdcInsn(fc.name());

        List<Expr> args = fc.args();
        mv.visitLdcInsn(args.size());
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        for (int i = 0; i < args.size(); i++) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn(i);
            emitExpr(args.get(i));
            mv.visitInsn(AASTORE);
        }

        mv.visitMethodInsn(INVOKESTATIC, RUNTIME, "callFunction",
                "(Ljava/lang/String;[Ljava/lang/Object;)" +
                "Ljava/lang/Object;",
                false);
    }

    private void emitBinOp(BinOp op) {
        emitExpr(op.left());
        emitExpr(op.right());
        String method = switch (op.op()) {
            case "+"  -> "add";
            case "-"  -> "sub";
            case "*"  -> "mul";
            case "/"  -> "div";
            case "%"  -> "mod";
            case "==" -> "eq";
            case "!=" -> "neq";
            case "<"  -> "lt";
            case "<=" -> "lte";
            case ">"  -> "gt";
            case ">=" -> "gte";
            default   -> throw new RuntimeException("Unknown op: " + op.op());
        };
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME, method,
                "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void emitDefaultConstructor() {
        MethodVisitor ctor = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        ctor.visitCode();
        ctor.visitVarInsn(ALOAD, 0);
        ctor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        ctor.visitInsn(RETURN);
        ctor.visitMaxs(1, 1);
        ctor.visitEnd();
    }

    private int getOrCreateSlot(String name) {
        return locals.computeIfAbsent(name, k -> nextSlot++);
    }
}
