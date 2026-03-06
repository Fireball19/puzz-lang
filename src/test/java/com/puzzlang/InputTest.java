package com.puzzlang;

import com.puzzlang.api.CompiledUnit;
import com.puzzlang.api.PuzzCompiler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for input handling features:
 *   - read("file.txt")
 *   - stdin()
 *   - .lines()
 *   - .paragraphs()
 *   - .split()
 *   - .words()
 *   - .chars()
 *   - PuzzList iteration and methods
 */
class InputTest {

    @TempDir
    Path tempDir;

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

    private String runWithStdin(String source, String stdinContent) throws Exception {
        PuzzCompiler compiler = new PuzzCompiler();
        CompiledUnit unit = compiler.compile(source, "T");

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        InputStream oldIn = System.in;

        System.setOut(new PrintStream(buf));
        System.setIn(new ByteArrayInputStream(stdinContent.getBytes()));
        try {
            unit.execute();
        } finally {
            System.setOut(oldOut);
            System.setIn(oldIn);
        }
        return buf.toString().stripTrailing();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  read() tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void readFileSimple() throws Exception {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "hello world");

        String src = "let data = read(\"" + file.toString().replace("\\", "\\\\") + "\")\n" +
                "print data\n";
        assertEquals("hello world", run(src));
    }

    @Test
    void readFileMultiline() throws Exception {
        Path file = tempDir.resolve("multi.txt");
        Files.writeString(file, "line1\nline2\nline3");

        String src = "let data = read(\"" + file.toString().replace("\\", "\\\\") + "\")\n" +
                "print data.lines().count()\n";
        assertEquals("3", run(src));
    }

    @Test
    void readFileNotFound() throws Exception {
        String src = "let data = read(\"nonexistent.txt\")\nprint data\n";
        assertThrows(Exception.class, () -> run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  stdin() tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void stdinSimple() throws Exception {
        String src = "let data = stdin()\nprint data\n";
        assertEquals("hello from stdin", runWithStdin(src, "hello from stdin"));
    }

    @Test
    void stdinMultiline() throws Exception {
        String src = "let data = stdin()\nprint data.lines().count()\n";
        assertEquals("3", runWithStdin(src, "a\nb\nc"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  .lines() tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void linesBasic() throws Exception {
        Path file = tempDir.resolve("lines.txt");
        Files.writeString(file, "a\nb\nc");

        String src = "let lines = read(\"" + file.toString().replace("\\", "\\\\") + "\").lines()\n" +
                "print lines.count()\n";
        assertEquals("3", run(src));
    }

    @Test
    void linesIteration() throws Exception {
        Path file = tempDir.resolve("iter.txt");
        Files.writeString(file, "x\ny\nz");

        String src = "for line in read(\"" + file.toString().replace("\\", "\\\\") + "\").lines():\n" +
                "    print line\n";
        assertEquals("x\ny\nz", run(src));
    }

    @Test
    void linesFirst() throws Exception {
        Path file = tempDir.resolve("first.txt");
        Files.writeString(file, "first\nsecond\nthird");

        String src = "print read(\"" + file.toString().replace("\\", "\\\\") + "\").lines().first()\n";
        assertEquals("first", run(src));
    }

    @Test
    void linesLast() throws Exception {
        Path file = tempDir.resolve("last.txt");
        Files.writeString(file, "first\nsecond\nthird");

        String src = "print read(\"" + file.toString().replace("\\", "\\\\") + "\").lines().last()\n";
        assertEquals("third", run(src));
    }

    @Test
    void linesGet() throws Exception {
        Path file = tempDir.resolve("get.txt");
        Files.writeString(file, "a\nb\nc\nd");

        String src = "print read(\"" + file.toString().replace("\\", "\\\\") + "\").lines().get(2)\n";
        assertEquals("c", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  .paragraphs() tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void paragraphsBasic() throws Exception {
        Path file = tempDir.resolve("para.txt");
        Files.writeString(file, "group1\n\ngroup2\n\ngroup3");

        String src = "let data = read(\"" + file.toString().replace("\\", "\\\\") + "\")\n" +
                "print data.paragraphs().count()\n";
        assertEquals("3", run(src));
    }

    @Test
    void paragraphsIteration() throws Exception {
        Path file = tempDir.resolve("para2.txt");
        Files.writeString(file, "A\n\nB\n\nC");

        String src = "let data = read(\"" + file.toString().replace("\\", "\\\\") + "\")\n" +
                "for p in data.paragraphs():\n" +
                "    print p\n";
        assertEquals("A\nB\nC", run(src));
    }

    @Test
    void paragraphsMultilineGroups() throws Exception {
        Path file = tempDir.resolve("para3.txt");
        Files.writeString(file, "line1\nline2\n\nline3\nline4");

        String src = "let data = read(\"" + file.toString().replace("\\", "\\\\") + "\")\n" +
                "let paras = data.paragraphs()\n" +
                "print paras.count()\n" +
                "print paras.first()\n";
        assertEquals("2\nline1\nline2", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  .split() tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void splitComma() throws Exception {
        Path file = tempDir.resolve("csv.txt");
        Files.writeString(file, "a,b,c");

        String src = "let parts = read(\"" + file.toString().replace("\\", "\\\\") + "\").split(\",\")\n" +
                "print parts.count()\n" +
                "print parts.get(1)\n";
        assertEquals("3\nb", run(src));
    }

    @Test
    void splitIteration() throws Exception {
        Path file = tempDir.resolve("colon.txt");
        Files.writeString(file, "1:2:3");

        String src = "for part in read(\"" + file.toString().replace("\\", "\\\\") + "\").split(\":\"):\n" +
                "    print part\n";
        assertEquals("1\n2\n3", run(src));
    }

    @Test
    void splitJoin() throws Exception {
        Path file = tempDir.resolve("dash.txt");
        Files.writeString(file, "a-b-c");

        String src = "let parts = read(\"" + file.toString().replace("\\", "\\\\") + "\").split(\"-\")\n" +
                "print parts.join(\" | \")\n";
        assertEquals("a | b | c", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  .words() tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void wordsBasic() throws Exception {
        Path file = tempDir.resolve("words.txt");
        Files.writeString(file, "hello   world  foo");

        String src = "let w = read(\"" + file.toString().replace("\\", "\\\\") + "\").words()\n" +
                "print w.count()\n" +
                "print w.get(1)\n";
        assertEquals("3\nworld", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  .chars() tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void charsBasic() throws Exception {
        Path file = tempDir.resolve("chars.txt");
        Files.writeString(file, "abc");

        String src = "let c = read(\"" + file.toString().replace("\\", "\\\\") + "\").chars()\n" +
                "print c.count()\n" +
                "print c.get(1)\n";
        assertEquals("3\nb", run(src));
    }

    @Test
    void charsIteration() throws Exception {
        Path file = tempDir.resolve("xyz.txt");
        Files.writeString(file, "XYZ");

        String src = "for ch in read(\"" + file.toString().replace("\\", "\\\\") + "\").chars():\n" +
                "    print ch\n";
        assertEquals("X\nY\nZ", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PuzzList method tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void listIsEmpty() throws Exception {
        Path file = tempDir.resolve("empty.txt");
        Files.writeString(file, "");

        String src = "print read(\"" + file.toString().replace("\\", "\\\\") + "\").lines().isEmpty()\n";
        assertEquals("true", run(src));
    }

    @Test
    void listNotEmpty() throws Exception {
        Path file = tempDir.resolve("notempty.txt");
        Files.writeString(file, "a");

        String src = "print read(\"" + file.toString().replace("\\", "\\\\") + "\").lines().isEmpty()\n";
        assertEquals("false", run(src));
    }

    @Test
    void listReversed() throws Exception {
        Path file = tempDir.resolve("rev.txt");
        Files.writeString(file, "a\nb\nc");

        String src = "let r = read(\"" + file.toString().replace("\\", "\\\\") + "\").lines().reversed()\n" +
                "print r.join(\"-\")\n";
        assertEquals("c-b-a", run(src));
    }

    @Test
    void listContains() throws Exception {
        Path file = tempDir.resolve("fruits.txt");
        Files.writeString(file, "apple\nbanana\ncherry");

        String src = "let items = read(\"" + file.toString().replace("\\", "\\\\") + "\").lines()\n" +
                "print items.contains(\"banana\")\n" +
                "print items.contains(\"grape\")\n";
        assertEquals("true\nfalse", run(src));
    }

    @Test
    void listIndexOf() throws Exception {
        Path file = tempDir.resolve("idx.txt");
        Files.writeString(file, "x\ny\nz");

        String src = "let items = read(\"" + file.toString().replace("\\", "\\\\") + "\").lines()\n" +
                "print items.indexOf(\"y\")\n" +
                "print items.indexOf(\"w\")\n";
        assertEquals("1\n-1", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  String method tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void stringTrim() throws Exception {
        Path file = tempDir.resolve("trim.txt");
        Files.writeString(file, "  hello  ");

        String src = "print read(\"" + file.toString().replace("\\", "\\\\") + "\").trim()\n";
        assertEquals("hello", run(src));
    }

    @Test
    void stringLength() throws Exception {
        Path file = tempDir.resolve("len.txt");
        Files.writeString(file, "hello");

        String src = "print read(\"" + file.toString().replace("\\", "\\\\") + "\").length()\n";
        assertEquals("5", run(src));
    }

    @Test
    void stringUpper() throws Exception {
        Path file = tempDir.resolve("upper.txt");
        Files.writeString(file, "hello");

        String src = "print read(\"" + file.toString().replace("\\", "\\\\") + "\").upper()\n";
        assertEquals("HELLO", run(src));
    }

    @Test
    void stringLower() throws Exception {
        Path file = tempDir.resolve("lower.txt");
        Files.writeString(file, "HELLO");

        String src = "print read(\"" + file.toString().replace("\\", "\\\\") + "\").lower()\n";
        assertEquals("hello", run(src));
    }

    @Test
    void stringStartsWith() throws Exception {
        Path file = tempDir.resolve("starts.txt");
        Files.writeString(file, "hello world");

        String src = "let s = read(\"" + file.toString().replace("\\", "\\\\") + "\")\n" +
                "print s.startsWith(\"hello\")\n" +
                "print s.startsWith(\"world\")\n";
        assertEquals("true\nfalse", run(src));
    }

    @Test
    void stringEndsWith() throws Exception {
        Path file = tempDir.resolve("ends.txt");
        Files.writeString(file, "hello world");

        String src = "let s = read(\"" + file.toString().replace("\\", "\\\\") + "\")\n" +
                "print s.endsWith(\"world\")\n" +
                "print s.endsWith(\"hello\")\n";
        assertEquals("true\nfalse", run(src));
    }

    @Test
    void stringContains() throws Exception {
        Path file = tempDir.resolve("contains.txt");
        Files.writeString(file, "hello world");

        String src = "let s = read(\"" + file.toString().replace("\\", "\\\\") + "\")\n" +
                "print s.contains(\"lo wo\")\n" +
                "print s.contains(\"xyz\")\n";
        assertEquals("true\nfalse", run(src));
    }

    @Test
    void stringReplace() throws Exception {
        Path file = tempDir.resolve("replace.txt");
        Files.writeString(file, "hello world");

        String src = "print read(\"" + file.toString().replace("\\", "\\\\") + "\").replace(\"world\", \"puzz\")\n";
        assertEquals("hello puzz", run(src));
    }

    @Test
    void stringToInt() throws Exception {
        Path file = tempDir.resolve("toint.txt");
        Files.writeString(file, "42");

        String src = "let n = read(\"" + file.toString().replace("\\", "\\\\") + "\").toInt()\nprint n + 8\n";
        assertEquals("50", run(src));
    }

    @Test
    void stringGet() throws Exception {
        Path file = tempDir.resolve("getchar.txt");
        Files.writeString(file, "hello");

        String src = "print read(\"" + file.toString().replace("\\", "\\\\") + "\").get(1)\n";
        assertEquals("e", run(src));
    }

    @Test
    void stringSubstring() throws Exception {
        Path file = tempDir.resolve("substr.txt");
        Files.writeString(file, "hello world");

        String src = "let s = read(\"" + file.toString().replace("\\", "\\\\") + "\")\n" +
                "print s.substring(0, 5)\n" +
                "print s.substring(6)\n";
        assertEquals("hello\nworld", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Chaining tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void chainLinesCount() throws Exception {
        Path file = tempDir.resolve("chain.txt");
        Files.writeString(file, "1\n2\n3\n4\n5");

        String src = "print read(\"" + file.toString().replace("\\", "\\\\") + "\").lines().count()\n";
        assertEquals("5", run(src));
    }

    @Test
    void chainSplitFirst() throws Exception {
        Path file = tempDir.resolve("chainsplit.txt");
        Files.writeString(file, "a,b,c");

        String src = "print read(\"" + file.toString().replace("\\", "\\\\") + "\").split(\",\").first()\n";
        assertEquals("a", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  AoC-style integration tests
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void aocStyleSumLines() throws Exception {
        // Simulate summing numbers from input file
        Path file = tempDir.resolve("numbers.txt");
        Files.writeString(file, "10\n20\n30\n40");

        String src = "let total = 0\n" +
                "for line in read(\"" + file.toString().replace("\\", "\\\\") + "\").lines():\n" +
                "    let total = total + line.toInt()\n" +
                "print total\n";
        assertEquals("100", run(src));
    }

    @Test
    void aocStyleParagraphGroups() throws Exception {
        // Simulate processing paragraph-separated groups (common AoC pattern)
        Path file = tempDir.resolve("groups.txt");
        Files.writeString(file, "1\n2\n3\n\n4\n5\n\n6");

        String src = "let groups = read(\"" + file.toString().replace("\\", "\\\\") + "\").paragraphs()\n" +
                "print \"Groups: \" + groups.count()\n" +
                "print \"First group: \" + groups.first()\n";
        assertEquals("Groups: 3\nFirst group: 1\n2\n3", run(src));
    }

    @Test
    void aocStyleCommaSeparated() throws Exception {
        // Simulate processing comma-separated values
        Path file = tempDir.resolve("csv2.txt");
        Files.writeString(file, "1,2,3,4,5");

        String src = "let nums = read(\"" + file.toString().replace("\\", "\\\\") + "\").split(\",\")\n" +
                "let sum = 0\n" +
                "for n in nums:\n" +
                "    let sum = sum + n.toInt()\n" +
                "print sum\n";
        assertEquals("15", run(src));
    }
}