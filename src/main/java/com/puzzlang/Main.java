package com.puzzlang;

import com.puzzlang.compiler.Compiler;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            String source    = Files.readString(Path.of(args[0]));
            String className = Path.of(args[0]).getFileName()
                    .toString().replace(".puzz", "");
            Compiler c = new Compiler();
            c.execute(c.compile(source, className), className);
            return;
        }
        runDemo();
    }

    static void runDemo() throws Exception {
        String source =
                "# Ranges demo\n" +
                        "let r = 1..5\n" +
                        "print r\n" +
                        "print r.sum()\n" +
                        "print r.count()\n" +
                        "print r.min()\n" +
                        "print r.max()\n" +
                        "print r.contains(5)\n" +
                        "print r.contains(11)\n";

        System.out.println("=== Source ===\n" + source);
        System.out.println("=== Output ===");
        Compiler c = new Compiler();
        c.execute(c.compile(source, "Demo"), "Demo");
    }
}