package com.puzzlang;

import com.puzzlang.api.CompiledUnit;
import com.puzzlang.api.PuzzCompiler;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for String methods in PuzzLang:
 *   - lines(), paragraphs(), split(), words(), chars()
 *   - trim(), length(), upper(), lower()
 *   - startsWith(), endsWith(), contains(), replace()
 *   - toInt(), get(), substring(), isEmpty()
 */
class StringTest {

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
    //  lines() tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void linesSimple() throws Exception {
        String src = "let lines = \"a\\nb\\nc\".lines()\nprint lines.count()\n";
        assertEquals("3", run(src));
    }

    @Test
    void linesIterate() throws Exception {
        String src = "for line in \"x\\ny\\nz\".lines():\n    print line\n";
        assertEquals("x\ny\nz", run(src));
    }

    @Test
    void linesEmpty() throws Exception {
        String src = "print \"\".lines().count()\n";
        assertEquals("0", run(src));
    }

    @Test
    void linesSingleLine() throws Exception {
        String src = "print \"hello\".lines().count()\n";
        assertEquals("1", run(src));
    }

    @Test
    void linesFirst() throws Exception {
        String src = "print \"first\\nsecond\\nthird\".lines().first()\n";
        assertEquals("first", run(src));
    }

    @Test
    void linesLast() throws Exception {
        String src = "print \"first\\nsecond\\nthird\".lines().last()\n";
        assertEquals("third", run(src));
    }

    @Test
    void linesGet() throws Exception {
        String src = "print \"a\\nb\\nc\\nd\".lines().get(2)\n";
        assertEquals("c", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  paragraphs() tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void paragraphsSimple() throws Exception {
        String src = "let p = \"a\\n\\nb\\n\\nc\".paragraphs()\nprint p.count()\n";
        assertEquals("3", run(src));
    }

    @Test
    void paragraphsIterate() throws Exception {
        String src = "for p in \"X\\n\\nY\\n\\nZ\".paragraphs():\n    print p\n";
        assertEquals("X\nY\nZ", run(src));
    }

    @Test
    void paragraphsMultiline() throws Exception {
        String src = "let p = \"line1\\nline2\\n\\nline3\\nline4\".paragraphs()\n" +
                     "print p.count()\n" +
                     "print p.first()\n";
        assertEquals("2\nline1\nline2", run(src));
    }

    @Test
    void paragraphsEmpty() throws Exception {
        String src = "print \"\".paragraphs().count()\n";
        assertEquals("0", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  split() tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void splitComma() throws Exception {
        String src = "let parts = \"a,b,c\".split(\",\")\n" +
                     "print parts.count()\n" +
                     "print parts.get(1)\n";
        assertEquals("3\nb", run(src));
    }

    @Test
    void splitIterate() throws Exception {
        String src = "for p in \"1:2:3\".split(\":\"):\n    print p\n";
        assertEquals("1\n2\n3", run(src));
    }

    @Test
    void splitSingleChar() throws Exception {
        String src = "print \"a-b-c\".split(\"-\").join(\" \")\n";
        assertEquals("a b c", run(src));
    }

    @Test
    void splitMultiChar() throws Exception {
        String src = "print \"a::b::c\".split(\"::\").count()\n";
        assertEquals("3", run(src));
    }

    @Test
    void splitEmpty() throws Exception {
        String src = "print \"\".split(\",\").count()\n";
        assertEquals("0", run(src));
    }

    @Test
    void splitNoMatch() throws Exception {
        String src = "print \"abc\".split(\",\").count()\n";
        assertEquals("1", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  words() tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void wordsSimple() throws Exception {
        String src = "let w = \"hello world foo\".words()\n" +
                     "print w.count()\n" +
                     "print w.get(1)\n";
        assertEquals("3\nworld", run(src));
    }

    @Test
    void wordsMultipleSpaces() throws Exception {
        String src = "print \"a   b    c\".words().count()\n";
        assertEquals("3", run(src));
    }

    @Test
    void wordsWithTabs() throws Exception {
        String src = "print \"a\\tb\\tc\".words().count()\n";
        assertEquals("3", run(src));
    }

    @Test
    void wordsIterate() throws Exception {
        String src = "for w in \"one two three\".words():\n    print w\n";
        assertEquals("one\ntwo\nthree", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  chars() tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void charsSimple() throws Exception {
        String src = "let c = \"abc\".chars()\n" +
                     "print c.count()\n" +
                     "print c.get(1)\n";
        assertEquals("3\nb", run(src));
    }

    @Test
    void charsIterate() throws Exception {
        String src = "for c in \"XYZ\".chars():\n    print c\n";
        assertEquals("X\nY\nZ", run(src));
    }

    @Test
    void charsEmpty() throws Exception {
        String src = "print \"\".chars().count()\n";
        assertEquals("0", run(src));
    }

    @Test
    void charsJoin() throws Exception {
        String src = "print \"abc\".chars().join(\"-\")\n";
        assertEquals("a-b-c", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  trim() tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void trimSpaces() throws Exception {
        String src = "print \"  hello  \".trim()\n";
        assertEquals("hello", run(src));
    }

    @Test
    void trimTabs() throws Exception {
        String src = "print \"\\thello\\t\".trim()\n";
        assertEquals("hello", run(src));
    }

    @Test
    void trimNoChange() throws Exception {
        String src = "print \"hello\".trim()\n";
        assertEquals("hello", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  length() tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void lengthSimple() throws Exception {
        String src = "print \"hello\".length()\n";
        assertEquals("5", run(src));
    }

    @Test
    void lengthEmpty() throws Exception {
        String src = "print \"\".length()\n";
        assertEquals("0", run(src));
    }

    @Test
    void lengthWithSpaces() throws Exception {
        String src = "print \"hello world\".length()\n";
        assertEquals("11", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  upper() / lower() tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void upperSimple() throws Exception {
        String src = "print \"hello\".upper()\n";
        assertEquals("HELLO", run(src));
    }

    @Test
    void upperMixed() throws Exception {
        String src = "print \"Hello World\".upper()\n";
        assertEquals("HELLO WORLD", run(src));
    }

    @Test
    void lowerSimple() throws Exception {
        String src = "print \"HELLO\".lower()\n";
        assertEquals("hello", run(src));
    }

    @Test
    void lowerMixed() throws Exception {
        String src = "print \"Hello World\".lower()\n";
        assertEquals("hello world", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  startsWith() / endsWith() tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void startsWithTrue() throws Exception {
        String src = "print \"hello world\".startsWith(\"hello\")\n";
        assertEquals("true", run(src));
    }

    @Test
    void startsWithFalse() throws Exception {
        String src = "print \"hello world\".startsWith(\"world\")\n";
        assertEquals("false", run(src));
    }

    @Test
    void endsWithTrue() throws Exception {
        String src = "print \"hello world\".endsWith(\"world\")\n";
        assertEquals("true", run(src));
    }

    @Test
    void endsWithFalse() throws Exception {
        String src = "print \"hello world\".endsWith(\"hello\")\n";
        assertEquals("false", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  contains() tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void containsTrue() throws Exception {
        String src = "print \"hello world\".contains(\"lo wo\")\n";
        assertEquals("true", run(src));
    }

    @Test
    void containsFalse() throws Exception {
        String src = "print \"hello world\".contains(\"xyz\")\n";
        assertEquals("false", run(src));
    }

    @Test
    void containsStart() throws Exception {
        String src = "print \"hello\".contains(\"hel\")\n";
        assertEquals("true", run(src));
    }

    @Test
    void containsEnd() throws Exception {
        String src = "print \"hello\".contains(\"llo\")\n";
        assertEquals("true", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  replace() tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void replaceSimple() throws Exception {
        String src = "print \"hello world\".replace(\"world\", \"puzz\")\n";
        assertEquals("hello puzz", run(src));
    }

    @Test
    void replaceMultiple() throws Exception {
        String src = "print \"ababa\".replace(\"a\", \"x\")\n";
        assertEquals("xbxbx", run(src));
    }

    @Test
    void replaceNoMatch() throws Exception {
        String src = "print \"hello\".replace(\"xyz\", \"abc\")\n";
        assertEquals("hello", run(src));
    }

    @Test
    void replaceEmpty() throws Exception {
        String src = "print \"hello\".replace(\"l\", \"\")\n";
        assertEquals("heo", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  toInt() tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void toIntSimple() throws Exception {
        String src = "let n = \"42\".toInt()\nprint n + 8\n";
        assertEquals("50", run(src));
    }

    @Test
    void toIntNegative() throws Exception {
        String src = "let n = \"-10\".toInt()\nprint n + 15\n";
        assertEquals("5", run(src));
    }

    @Test
    void toIntWithSpaces() throws Exception {
        String src = "let n = \"  123  \".toInt()\nprint n\n";
        assertEquals("123", run(src));
    }

    @Test
    void toIntZero() throws Exception {
        String src = "print \"0\".toInt()\n";
        assertEquals("0", run(src));
    }

    @Test
    void toIntInvalid() throws Exception {
        String src = "print \"abc\".toInt()\n";
        assertThrows(Exception.class, () -> run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  get() tests (character access)
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void getFirst() throws Exception {
        String src = "print \"hello\".get(0)\n";
        assertEquals("h", run(src));
    }

    @Test
    void getMiddle() throws Exception {
        String src = "print \"hello\".get(2)\n";
        assertEquals("l", run(src));
    }

    @Test
    void getLast() throws Exception {
        String src = "print \"hello\".get(4)\n";
        assertEquals("o", run(src));
    }

    @Test
    void getOutOfBounds() throws Exception {
        String src = "print \"hello\".get(10)\n";
        assertThrows(Exception.class, () -> run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  substring() tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void substringFromStart() throws Exception {
        String src = "print \"hello world\".substring(0, 5)\n";
        assertEquals("hello", run(src));
    }

    @Test
    void substringFromMiddle() throws Exception {
        String src = "print \"hello world\".substring(6, 11)\n";
        assertEquals("world", run(src));
    }

    @Test
    void substringToEnd() throws Exception {
        String src = "print \"hello world\".substring(6)\n";
        assertEquals("world", run(src));
    }

    @Test
    void substringEmpty() throws Exception {
        String src = "print \"hello\".substring(2, 2)\n";
        assertEquals("", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  isEmpty() tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void isEmptyTrue() throws Exception {
        String src = "print \"\".isEmpty()\n";
        assertEquals("true", run(src));
    }

    @Test
    void isEmptyFalse() throws Exception {
        String src = "print \"hello\".isEmpty()\n";
        assertEquals("false", run(src));
    }

    @Test
    void isEmptySpace() throws Exception {
        String src = "print \" \".isEmpty()\n";
        assertEquals("false", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Chaining tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void chainTrimUpper() throws Exception {
        String src = "print \"  hello  \".trim().upper()\n";
        assertEquals("HELLO", run(src));
    }

    @Test
    void chainSplitFirst() throws Exception {
        String src = "print \"a,b,c\".split(\",\").first()\n";
        assertEquals("a", run(src));
    }

    @Test
    void chainLinesCount() throws Exception {
        String src = "print \"a\\nb\\nc\".lines().count()\n";
        assertEquals("3", run(src));
    }

    @Test
    void chainReplaceLength() throws Exception {
        String src = "print \"hello\".replace(\"l\", \"LL\").length()\n";
        assertEquals("7", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Operator precedence tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void methodCallPrecedenceOverAdd() throws Exception {
        String src = "print \"prefix: \" + \"hello\".upper()\n";
        assertEquals("prefix: HELLO", run(src));
    }

    @Test
    void methodCallPrecedenceWithCount() throws Exception {
        String src = "print \"count: \" + \"a,b,c\".split(\",\").count()\n";
        assertEquals("count: 3", run(src));
    }
}
