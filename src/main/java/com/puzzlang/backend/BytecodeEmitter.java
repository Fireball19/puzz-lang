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
 *   - Method calls: INVOKESTATIC PuzzRuntime.callMethod
 *   - For-in loops: get iterator, loop with hasNext/next
 */
public class BytecodeEmitter {

    private static final String RUNTIME = "com/puzzlang/runtime/PuzzRuntime";

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
            case ExprStmt es  -> { emitExpr(es.expr()); mv.visitInsn(POP); }
            case Program p    -> p.stmts().forEach(this::emitStmt);
        }
    }

    private void emitVarDecl(VarDecl vd) {
        emitExpr(vd.value());
        mv.visitVarInsn(ASTORE, getOrCreateSlot(vd.name()));
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
     * for VAR in ITERABLE: BODY
     *
     * Compiles to:
     *   iter = PuzzRuntime.toIterable(ITERABLE).iterator()
     *   loopStart:
     *     if !iter.hasNext() goto loopEnd
     *     VAR = (Object) iter.next()
     *     BODY
     *     goto loopStart
     *   loopEnd:
     */
    private void emitForIn(ForIn fi) {
        // Allocate a hidden slot for the iterator (not user-visible)
        int iterSlot = nextSlot++;

        // ── Get iterator ──────────────────────────────────────────────────
        emitExpr(fi.iterable());
        // PuzzRuntime.toIterable(val) → Iterable<Object>
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME, "toIterable",
                "(Ljava/lang/Object;)Ljava/lang/Iterable;", false);
        // iterable.iterator() → Iterator<Object>
        mv.visitMethodInsn(INVOKEINTERFACE, "java/lang/Iterable", "iterator",
                "()Ljava/util/Iterator;", true);
        mv.visitVarInsn(ASTORE, iterSlot);

        // ── Loop ──────────────────────────────────────────────────────────
        Label loopStart = new Label();
        Label loopEnd   = new Label();

        mv.visitLabel(loopStart);

        // iter.hasNext()
        mv.visitVarInsn(ALOAD, iterSlot);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext",
                "()Z", true);
        mv.visitJumpInsn(IFEQ, loopEnd);  // if false → loopEnd

        // VAR = iter.next()
        mv.visitVarInsn(ALOAD, iterSlot);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next",
                "()Ljava/lang/Object;", true);
        mv.visitVarInsn(ASTORE, getOrCreateSlot(fi.var()));

        // Body
        for (Stmt s : fi.body()) emitStmt(s);

        mv.visitJumpInsn(GOTO, loopStart);
        mv.visitLabel(loopEnd);
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
                // Stack:  start, end
                // Call:   PuzzRuntime.makeRange(Object, Object) → Object
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

            // ── Method calls: receiver.method(arg0, arg1, ...) ───────────
            case MethodCall mc -> emitMethodCall(mc);
        }
    }

    /**
     * Compiles  receiver.method(arg0, arg1, ...)
     *
     * We build an Object[] for the varargs and call:
     *   PuzzRuntime.callMethod(Object receiver, String method, Object... args)
     */
    private void emitMethodCall(MethodCall mc) {
        emitExpr(mc.receiver());                        // receiver
        mv.visitLdcInsn(mc.method());                   // method name

        // Build Object[] args array
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
                "(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;",
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
