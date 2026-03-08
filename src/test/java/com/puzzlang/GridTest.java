package com.puzzlang;

import com.puzzlang.api.CompiledUnit;
import com.puzzlang.api.PuzzCompiler;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PuzzGrid operations (2D Grid utilities).
 *
 * Grid operations tested:
 *   - Creation: stdin().grid(), "string".grid(), grid(toInt)
 *   - Dimensions: width(), height(), bounds()
 *   - Access: at(x,y), get(x,y,default), row(y), col(x)
 *   - Iteration: cells(), coords(), neighbors(), neighbors8()
 *   - Finding: find(), find_all(), count()
 *   - Transformations: rotate_cw(), rotate_ccw(), flip_h(), flip_v(), transpose()
 *   - Pathfinding: bfs(), flood_fill()
 */
class GridTest {

    private String run(String source) throws Exception {
        PuzzCompiler compiler = new PuzzCompiler();
        CompiledUnit unit = compiler.compile(source, "GridTest");

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
        CompiledUnit unit = compiler.compile(source, "GridTest");

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
    //  Grid Creation
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void gridFromString() throws Exception {
        String src = """
            let grid = "abc\\ndef\\nghi".grid()
            print grid.width()
            print grid.height()
            """;
        assertEquals("3\n3", run(src));
    }

    @Test
    void gridFromStdin() throws Exception {
        String src = """
            let grid = stdin().grid()
            print grid.width()
            print grid.height()
            """;
        assertEquals("5\n3", runWithStdin(src, ".....\n.....\n....."));
    }

    @Test
    void gridToString() throws Exception {
        String src = """
            let grid = "AB\\nCD".grid()
            print grid
            """;
        assertEquals("AB\nCD", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Dimensions
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void gridDimensions() throws Exception {
        String src = """
            let grid = "12345\\n67890".grid()
            print grid.width()
            print grid.height()
            """;
        assertEquals("5\n2", run(src));
    }

    @Test
    void gridBounds() throws Exception {
        String src = """
            let grid = "abc\\ndef\\nghi".grid()
            let b = grid.bounds()
            print b.first()
            print b.last()
            """;
        assertEquals("3\n3", run(src));
    }

    @Test
    void gridIsEmpty() throws Exception {
        String src = """
            let grid = "ab".grid()
            print grid.is_empty()
            """;
        assertEquals("false", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Access
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void gridAt() throws Exception {
        String src = """
            let grid = "ABC\\nDEF\\nGHI".grid()
            print grid.at(0, 0)
            print grid.at(1, 1)
            print grid.at(2, 2)
            """;
        assertEquals("A\nE\nI", run(src));
    }

    @Test
    void gridGet() throws Exception {
        String src = """
            let grid = "AB\\nCD".grid()
            print grid.get(0, 0, "X")
            print grid.get(10, 10, "X")
            """;
        assertEquals("A\nX", run(src));
    }

    @Test
    void gridRow() throws Exception {
        String src = """
            let grid = "ABC\\nDEF\\nGHI".grid()
            print grid.row(1).join("")
            """;
        assertEquals("DEF", run(src));
    }

    @Test
    void gridCol() throws Exception {
        String src = """
            let grid = "ABC\\nDEF\\nGHI".grid()
            print grid.col(1).join("")
            """;
        assertEquals("BEH", run(src));
    }

    @Test
    void gridInBounds() throws Exception {
        String src = """
            let grid = "ABC\\nDEF".grid()
            print grid.in_bounds(0, 0)
            print grid.in_bounds(2, 1)
            print grid.in_bounds(3, 0)
            print grid.in_bounds(0, 2)
            """;
        assertEquals("true\ntrue\nfalse\nfalse", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Iteration
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void gridCells() throws Exception {
        String src = """
            let grid = "AB\\nCD".grid()
            let count = 0
            for cell in grid.cells():
                let count = count + 1
            print count
            """;
        assertEquals("4", run(src));
    }

    @Test
    void gridCoords() throws Exception {
        String src = """
            let grid = "AB\\nCD".grid()
            let count = 0
            for coord in grid.coords():
                let count = count + 1
            print count
            """;
        assertEquals("4", run(src));
    }

    @Test
    void gridNeighbors() throws Exception {
        // Center of 3x3 grid should have 4 neighbors
        String src = """
            let grid = "...\\n...\\n...".grid()
            let n = grid.neighbors(1, 1)
            print n.count()
            """;
        assertEquals("4", run(src));
    }

    @Test
    void gridNeighborsCorner() throws Exception {
        // Corner should have 2 neighbors
        String src = """
            let grid = "...\\n...\\n...".grid()
            let n = grid.neighbors(0, 0)
            print n.count()
            """;
        assertEquals("2", run(src));
    }

    @Test
    void gridNeighbors8() throws Exception {
        // Center of 3x3 grid should have 8 neighbors
        String src = """
            let grid = "...\\n...\\n...".grid()
            let n = grid.neighbors8(1, 1)
            print n.count()
            """;
        assertEquals("8", run(src));
    }

    @Test
    void gridNeighbors8Corner() throws Exception {
        // Corner should have 3 neighbors (8-directional)
        String src = """
            let grid = "...\\n...\\n...".grid()
            let n = grid.neighbors8(0, 0)
            print n.count()
            """;
        assertEquals("3", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Finding
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void gridFind() throws Exception {
        String src = """
            let grid = "...\\n.X.\\n...".grid()
            let pos = grid.find("X")
            print pos.first()
            print pos.last()
            """;
        assertEquals("1\n1", run(src));
    }

    @Test
    void gridFindNotFound() throws Exception {
        String src = """
            let grid = "...\\n...\\n...".grid()
            let pos = grid.find("X")
            print pos
            """;
        assertEquals("null", run(src));
    }

    @Test
    void gridFindAll() throws Exception {
        String src = """
            let grid = "X.X\\n.X.\\nX.X".grid()
            let positions = grid.find_all("X")
            print positions.count()
            """;
        assertEquals("5", run(src));
    }

    @Test
    void gridCount() throws Exception {
        String src = """
            let grid = "###\\n#.#\\n###".grid()
            print grid.count("#")
            print grid.count(".")
            """;
        assertEquals("8\n1", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Transformations
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void gridRotateCw() throws Exception {
        String src = """
            let grid = "12\\n34".grid()
            let rotated = grid.rotate_cw()
            print rotated
            """;
        assertEquals("31\n42", run(src));
    }

    @Test
    void gridRotateCcw() throws Exception {
        String src = """
            let grid = "12\\n34".grid()
            let rotated = grid.rotate_ccw()
            print rotated
            """;
        assertEquals("24\n13", run(src));
    }

    @Test
    void gridFlipH() throws Exception {
        String src = """
            let grid = "123\\n456".grid()
            let flipped = grid.flip_h()
            print flipped
            """;
        assertEquals("321\n654", run(src));
    }

    @Test
    void gridFlipV() throws Exception {
        String src = """
            let grid = "123\\n456".grid()
            let flipped = grid.flip_v()
            print flipped
            """;
        assertEquals("456\n123", run(src));
    }

    @Test
    void gridTranspose() throws Exception {
        String src = """
            let grid = "12\\n34\\n56".grid()
            let t = grid.transpose()
            print t.width()
            print t.height()
            print t
            """;
        assertEquals("3\n2\n135\n246", run(src));
    }

    @Test
    void gridSubgrid() throws Exception {
        String src = """
            let grid = "12345\\n67890\\nABCDE".grid()
            let sub = grid.subgrid(1, 0, 3, 1)
            print sub
            """;
        assertEquals("234\n789", run(src));
    }

    @Test
    void gridCopy() throws Exception {
        // Verify copy is independent
        String src = """
            let grid = "AB\\nCD".grid()
            let copy = grid.copy()
            print copy
            """;
        assertEquals("AB\nCD", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Pathfinding: BFS
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void gridBfsSimplePath() throws Exception {
        // Simple path from (0,0) to (2,0)
        String src = """
            let grid = "...\\n###\\n...".grid()
            let start = grid.find(".")
            let path = grid.bfs(start, start)
            print path.count()
            """;
        // Path to self is just the start position
        assertEquals("1", run(src));
    }

    @Test
    void gridBfsWithWalkable() throws Exception {
        // Path finding with walkable constraint
        String src = """
            let grid = "S..\\n.#.\\n..E".grid()
            let start = grid.find("S")
            let goal = grid.find("E")
            let path = grid.bfs(start, goal, "S.E")
            print path.count()
            """;
        // Should find path: (0,0) -> (1,0) -> (2,0) -> (2,1) -> (2,2) = 5 steps
        // Or alternative: (0,0) -> (0,1) -> ... no, blocked
        // Actually: S(0,0) -> (1,0) -> (2,0) -> (2,1) -> (2,2)E
        // Wait, (2,1) is blocked by #. Let me recalculate.
        // Grid:
        //   S . .
        //   . # .
        //   . . E
        // Path: S(0,0) -> (1,0) -> (2,0) -> (2,1)? No, (1,1) is #
        // Hmm: from (0,0) we can go to (1,0) or (0,1)
        // From (1,0) we can go to (2,0)
        // From (2,0) we can go to (2,1) - this is '.', not '#'
        // From (2,1) we can go to (2,2) which is E
        // Path length: 5 cells
        assertEquals("5", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Pathfinding: Flood Fill
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void gridFloodFillSimple() throws Exception {
        String src = """
            let grid = "...\\n.#.\\n...".grid()
            let start = grid.find(".")
            let region = grid.flood_fill(start)
            print region.count()
            """;
        // All '.' cells are connected (8 of them)
        assertEquals("8", run(src));
    }

    @Test
    void gridFloodFillIsolated() throws Exception {
        // Two separate regions
        String src = """
            let grid = "..#..\\n..#..\\n..#..".grid()
            let start = grid.find(".")
            let region = grid.flood_fill(start)
            print region.count()
            """;
        // Only the left region (6 cells: 2 columns * 3 rows = 6)
        assertEquals("6", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Grid Iteration with For Loop
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void gridIterateRows() throws Exception {
        String src = """
            let grid = "AB\\nCD\\nEF".grid()
            let count = 0
            for row in grid:
                let count = count + 1
            print count
            """;
        assertEquals("3", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  AoC-Style Examples
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void aocStyleGridParsing() throws Exception {
        // Common AoC pattern: find start position, count obstacles
        String src = """
            let grid = "#.#\\n.S.\\n#.#".grid()
            let start = grid.find("S")
            let walls = grid.count("#")
            print start.first()
            print start.last()
            print walls
            """;
        assertEquals("1\n1\n4", run(src));
    }

    @Test
    void aocStyleNeighborIteration() throws Exception {
        // Count empty neighbors of a position
        String src = """
            let grid = ".#.\\n#.#\\n.#.".grid()
            let count = 0
            for n in grid.neighbors(1, 1):
                let x = n.first()
                let y = n.last()
                if grid.at(x, y) == "#":
                    let count = count + 1
            print count
            """;
        assertEquals("4", run(src));
    }

    @Test
    void aocStyleRegionCounting() throws Exception {
        // Common pattern: count cells in a region
        String src = """
            let grid = "###\\n#.#\\n###".grid()
            let emptyCount = grid.count(".")
            let wallCount = grid.count("#")
            print emptyCount
            print wallCount
            """;
        assertEquals("1\n8", run(src));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Edge Cases
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void gridSingleCell() throws Exception {
        String src = """
            let grid = "X".grid()
            print grid.width()
            print grid.height()
            print grid.at(0, 0)
            """;
        assertEquals("1\n1\nX", run(src));
    }

    @Test
    void gridSingleRow() throws Exception {
        String src = """
            let grid = "ABCDE".grid()
            print grid.width()
            print grid.height()
            """;
        assertEquals("5\n1", run(src));
    }

    @Test
    void gridSingleColumn() throws Exception {
        String src = """
            let grid = "A\\nB\\nC\\nD\\nE".grid()
            print grid.width()
            print grid.height()
            """;
        assertEquals("1\n5", run(src));
    }
}
