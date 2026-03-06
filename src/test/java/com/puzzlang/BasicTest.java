package com.puzzlang;

import com.puzzlang.api.CompiledUnit;
import com.puzzlang.api.PuzzCompiler;
import com.puzzlang.diagnostic.CompilationException;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for basic PuzzLang language constructs:
 *   - Variables and assignment
 *   - Arithmetic operations
 *   - Comparison operators
 *   - Boolean literals and logic
 *   - If/else statements
 *   - While loops
 *   - For-in loops
 *   - Print statements
 *   - Comments
 */
class BasicTest {

    private String run(String source) throws Exception {
        PuzzCompiler compiler = new PuzzCompiler();
        CompiledUnit unit = compiler.compile(source, "T");
        
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream old = System.out;
        System.setOut(new PrintStream(buf));
        try {
            unit.execute();
        } finally {
            System.setOut(old);
        }
        return buf.toString().stripTrailing();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Literals
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void integerLiteral() throws Exception {
        assertEquals("42", run("print 42\n"));
    }

    @Test
    void negativeLiteral() throws Exception {
        assertEquals("-8", run("print 0 - 8\n"));
    }

    @Test
    void stringLiteral() throws Exception {
        assertEquals("hello", run("print \"hello\"\n"));
    }

    @Test
    void stringWithSpaces() throws Exception {
        assertEquals("hello world", run("print \"hello world\"\n"));
    }

    @Test
    void booleanTrue() throws Exception {
        assertEquals("true", run("print true\n"));
    }

    @Test
    void booleanFalse() throws Exception {
        assertEquals("false", run("print false\n"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Variables
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void variableDeclaration() throws Exception {
        String src = "let x = 10\nprint x\n";
        assertEquals("10", run(src));
    }

    @Test
    void variableReassignment() throws Exception {
        String src = "let x = 10\nlet x = 20\nprint x\n";
        assertEquals("20", run(src));
    }

    @Test
    void multipleVariables() throws Exception {
        String src = "let a = 1\nlet b = 2\nlet c = 3\nprint a + b + c\n";
        assertEquals("6", run(src));
    }

    @Test
    void variableWithExpression() throws Exception {
        String src = "let x = 5 + 3\nprint x\n";
        assertEquals("8", run(src));
    }

    @Test
    void variableStringConcat() throws Exception {
        String src = "let greeting = \"hello \" + \"world\"\nprint greeting\n";
        assertEquals("hello world", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Arithmetic operations
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void addition() throws Exception {
        assertEquals("15", run("print 10 + 5\n"));
    }

    @Test
    void subtraction() throws Exception {
        assertEquals("5", run("print 10 - 5\n"));
    }

    @Test
    void multiplication() throws Exception {
        assertEquals("50", run("print 10 * 5\n"));
    }

    @Test
    void division() throws Exception {
        assertEquals("2", run("print 10 / 5\n"));
    }

    @Test
    void integerDivision() throws Exception {
        assertEquals("3", run("print 10 / 3\n"));
    }

    @Test
    void divisionByZero() throws Exception {
        assertThrows(Exception.class, () -> run("print 10 / 0\n"));
    }

    @Test
    void operatorPrecedence() throws Exception {
        assertEquals("17", run("print 2 + 3 * 5\n"));
    }

    @Test
    void operatorPrecedenceWithParens() throws Exception {
        assertEquals("25", run("print (2 + 3) * 5\n"));
    }

    @Test
    void complexExpression() throws Exception {
        assertEquals("14", run("print 2 + 3 * 4\n"));
    }

    @Test
    void nestedParentheses() throws Exception {
        assertEquals("20", run("print ((2 + 3) * (1 + 1)) * 2\n"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  String concatenation
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void stringConcatStrings() throws Exception {
        assertEquals("hello world", run("print \"hello \" + \"world\"\n"));
    }

    @Test
    void stringConcatWithInt() throws Exception {
        assertEquals("value: 42", run("print \"value: \" + 42\n"));
    }

    @Test
    void intConcatWithString() throws Exception {
        assertEquals("42 is the answer", run("print 42 + \" is the answer\"\n"));
    }

    @Test
    void multipleConcats() throws Exception {
        assertEquals("a1b2c", run("print \"a\" + 1 + \"b\" + 2 + \"c\"\n"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Comparison operators
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void equalTrue() throws Exception {
        assertEquals("true", run("print 5 == 5\n"));
    }

    @Test
    void equalFalse() throws Exception {
        assertEquals("false", run("print 5 == 6\n"));
    }

    @Test
    void notEqualTrue() throws Exception {
        assertEquals("true", run("print 5 != 6\n"));
    }

    @Test
    void notEqualFalse() throws Exception {
        assertEquals("false", run("print 5 != 5\n"));
    }

    @Test
    void lessThanTrue() throws Exception {
        assertEquals("true", run("print 3 < 5\n"));
    }

    @Test
    void lessThanFalse() throws Exception {
        assertEquals("false", run("print 5 < 3\n"));
    }

    @Test
    void lessThanEqual() throws Exception {
        assertEquals("false", run("print 5 < 5\n"));
    }

    @Test
    void lessOrEqualTrue() throws Exception {
        assertEquals("true", run("print 5 <= 5\n"));
    }

    @Test
    void lessOrEqualLess() throws Exception {
        assertEquals("true", run("print 3 <= 5\n"));
    }

    @Test
    void lessOrEqualFalse() throws Exception {
        assertEquals("false", run("print 6 <= 5\n"));
    }

    @Test
    void greaterThanTrue() throws Exception {
        assertEquals("true", run("print 5 > 3\n"));
    }

    @Test
    void greaterThanFalse() throws Exception {
        assertEquals("false", run("print 3 > 5\n"));
    }

    @Test
    void greaterOrEqualTrue() throws Exception {
        assertEquals("true", run("print 5 >= 5\n"));
    }

    @Test
    void greaterOrEqualFalse() throws Exception {
        assertEquals("false", run("print 4 >= 5\n"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  If statements
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void ifTrue() throws Exception {
        String src = "if true:\n    print \"yes\"\n";
        assertEquals("yes", run(src));
    }

    @Test
    void ifFalse() throws Exception {
        String src = "if false:\n    print \"yes\"\nprint \"done\"\n";
        assertEquals("done", run(src));
    }

    @Test
    void ifElseTrue() throws Exception {
        String src = "if true:\n    print \"yes\"\nelse:\n    print \"no\"\n";
        assertEquals("yes", run(src));
    }

    @Test
    void ifElseFalse() throws Exception {
        String src = "if false:\n    print \"yes\"\nelse:\n    print \"no\"\n";
        assertEquals("no", run(src));
    }

    @Test
    void ifWithCondition() throws Exception {
        String src = "let x = 10\nif x > 5:\n    print \"big\"\nelse:\n    print \"small\"\n";
        assertEquals("big", run(src));
    }

    @Test
    void ifWithConditionFalse() throws Exception {
        String src = "let x = 3\nif x > 5:\n    print \"big\"\nelse:\n    print \"small\"\n";
        assertEquals("small", run(src));
    }

    @Test
    void nestedIf() throws Exception {
        String src = "let x = 10\n" +
                     "if x > 5:\n" +
                     "    if x > 8:\n" +
                     "        print \"very big\"\n" +
                     "    else:\n" +
                     "        print \"medium\"\n";
        assertEquals("very big", run(src));
    }

    @Test
    void ifMultipleStatements() throws Exception {
        String src = "if true:\n    print \"a\"\n    print \"b\"\n    print \"c\"\n";
        assertEquals("a\nb\nc", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  While loops
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void whileLoop() throws Exception {
        String src = "let i = 0\n" +
                     "while i < 3:\n" +
                     "    print i\n" +
                     "    let i = i + 1\n";
        assertEquals("0\n1\n2", run(src));
    }

    @Test
    void whileFalse() throws Exception {
        String src = "while false:\n    print \"never\"\nprint \"done\"\n";
        assertEquals("done", run(src));
    }

    @Test
    void whileCountdown() throws Exception {
        String src = "let n = 5\n" +
                     "while n > 0:\n" +
                     "    print n\n" +
                     "    let n = n - 1\n";
        assertEquals("5\n4\n3\n2\n1", run(src));
    }

    @Test
    void whileSum() throws Exception {
        String src = "let sum = 0\n" +
                     "let i = 1\n" +
                     "while i <= 5:\n" +
                     "    let sum = sum + i\n" +
                     "    let i = i + 1\n" +
                     "print sum\n";
        assertEquals("15", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  For-in loops (with ranges)
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void forInExclusive() throws Exception {
        String src = "for i in 0..3:\n    print i\n";
        assertEquals("0\n1\n2", run(src));
    }

    @Test
    void forInInclusive() throws Exception {
        String src = "for i in 1..=3:\n    print i\n";
        assertEquals("1\n2\n3", run(src));
    }

    @Test
    void forInSum() throws Exception {
        String src = "let sum = 0\n" +
                     "for i in 1..=5:\n" +
                     "    let sum = sum + i\n" +
                     "print sum\n";
        assertEquals("15", run(src));
    }

    @Test
    void forInNested() throws Exception {
        String src = "for i in 1..=2:\n" +
                     "    for j in 1..=2:\n" +
                     "        print i * 10 + j\n";
        assertEquals("11\n12\n21\n22", run(src));
    }

    @Test
    void forInWithRange() throws Exception {
        String src = "let r = 1..4\nfor x in r:\n    print x\n";
        assertEquals("1\n2\n3", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Truthy/falsy values
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void truthyInt() throws Exception {
        String src = "if 1:\n    print \"yes\"\nelse:\n    print \"no\"\n";
        assertEquals("yes", run(src));
    }

    @Test
    void falsyZero() throws Exception {
        String src = "if 0:\n    print \"yes\"\nelse:\n    print \"no\"\n";
        assertEquals("no", run(src));
    }

    @Test
    void truthyString() throws Exception {
        String src = "if \"hello\":\n    print \"yes\"\nelse:\n    print \"no\"\n";
        assertEquals("yes", run(src));
    }

    @Test
    void falsyEmptyString() throws Exception {
        String src = "if \"\":\n    print \"yes\"\nelse:\n    print \"no\"\n";
        assertEquals("no", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Comments
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void lineComment() throws Exception {
        String src = "# This is a comment\nprint 42\n";
        assertEquals("42", run(src));
    }

    @Test
    void inlineComment() throws Exception {
        String src = "let x = 10 # assign 10\nprint x\n";
        assertEquals("10", run(src));
    }

    @Test
    void multipleComments() throws Exception {
        String src = "# comment 1\n# comment 2\nprint \"hello\"\n# comment 3\n";
        assertEquals("hello", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Scoping
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void outerVariableVisibleInIf() throws Exception {
        String src = "let x = 10\nif true:\n    print x\n";
        assertEquals("10", run(src));
    }

    @Test
    void outerVariableVisibleInWhile() throws Exception {
        String src = "let x = 5\nlet i = 0\nwhile i < 1:\n    print x\n    let i = i + 1\n";
        assertEquals("5", run(src));
    }

    @Test
    void outerVariableVisibleInFor() throws Exception {
        String src = "let x = 100\nfor i in 1..=2:\n    print x + i\n";
        assertEquals("101\n102", run(src));
    }

    @Test
    void forLoopVariableOutOfScope() {
        String src = "for i in 1..3:\n    print i\nprint i\n";
        assertThrows(CompilationException.class, () -> run(src));
    }

    @Test
    void ifVariableOutOfScope() {
        String src = "if true:\n    let inner = 42\nprint inner\n";
        assertThrows(CompilationException.class, () -> run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Error handling
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void undefinedVariable() {
        String src = "print x\n";
        assertThrows(CompilationException.class, () -> run(src));
    }

    @Test
    void undefinedInExpression() {
        String src = "let y = x + 1\n";
        assertThrows(CompilationException.class, () -> run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Complex programs
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void factorial() throws Exception {
        String src = "let n = 5\n" +
                     "let result = 1\n" +
                     "while n > 0:\n" +
                     "    let result = result * n\n" +
                     "    let n = n - 1\n" +
                     "print result\n";
        assertEquals("120", run(src));
    }

    @Test
    void fibonacci() throws Exception {
        String src = "let a = 0\n" +
                     "let b = 1\n" +
                     "let i = 0\n" +
                     "while i < 10:\n" +
                     "    print a\n" +
                     "    let temp = a + b\n" +
                     "    let a = b\n" +
                     "    let b = temp\n" +
                     "    let i = i + 1\n";
        assertEquals("0\n1\n1\n2\n3\n5\n8\n13\n21\n34", run(src));
    }

    @Test
    void sumOfSquares() throws Exception {
        String src = "let sum = 0\n" +
                     "for i in 1..=5:\n" +
                     "    let sum = sum + i * i\n" +
                     "print sum\n";
        assertEquals("55", run(src));
    }

    @Test
    void fizzBuzzSingle() throws Exception {
        String src = "let n = 15\n" +
                     "if n / 3 * 3 == n:\n" +
                     "    if n / 5 * 5 == n:\n" +
                     "        print \"FizzBuzz\"\n" +
                     "    else:\n" +
                     "        print \"Fizz\"\n" +
                     "else:\n" +
                     "    if n / 5 * 5 == n:\n" +
                     "        print \"Buzz\"\n" +
                     "    else:\n" +
                     "        print n\n";
        assertEquals("FizzBuzz", run(src));
    }
}
