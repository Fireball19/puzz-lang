package com.puzzlang.runtime;

import java.util.*;
import java.util.function.Function;

/**
 * PuzzGrid — a first-class 2D grid value for PuzzLang.
 *
 * Grids appear in ~40% of Advent of Code problems, making this a killer feature.
 * Provides efficient grid operations including pathfinding algorithms.
 *
 * COORDINATE SYSTEM:
 *   - x = column (0 to width-1, left to right)
 *   - y = row (0 to height-1, top to bottom)
 *   - grid[y][x] or grid.at(x, y) for access
 *
 * SUPPORTED OPERATIONS:
 *
 *   Creation:
 *     stdin().grid()              → parse input as char grid
 *     stdin().grid(toInt)         → parse with mapper function
 *
 *   Access:
 *     grid.at(x, y)               → bounds-checked access
 *     grid.get(x, y, default)     → access with default for out-of-bounds
 *     grid.row(y)                 → get entire row as list
 *     grid.col(x)                 → get entire column as list
 *
 *   Dimensions:
 *     grid.width()                → number of columns
 *     grid.height()               → number of rows
 *     grid.bounds()               → (width, height) tuple
 *
 *   Iteration:
 *     grid.cells()                → iterable of (x, y, value) tuples
 *     grid.coords()               → iterable of (x, y) coordinate tuples
 *     grid.neighbors(x, y)        → 4-directional neighbors
 *     grid.neighbors8(x, y)       → 8-directional neighbors
 *
 *   Finding:
 *     grid.find(val)              → first (x, y) position of value
 *     grid.find_all(val)          → list of all positions
 *     grid.count(val)             → count occurrences
 *
 *   Transformations:
 *     grid.rotate_cw()            → rotate 90° clockwise
 *     grid.rotate_ccw()           → rotate 90° counter-clockwise
 *     grid.flip_h()               → flip horizontally
 *     grid.flip_v()               → flip vertically
 *     grid.transpose()            → swap rows and columns
 *     grid.subgrid(x1,y1,x2,y2)   → extract rectangular region
 *
 *   Pathfinding:
 *     grid.bfs(start, goal)       → shortest path BFS
 *     grid.bfs(start, goal, walkable) → BFS with walkable constraint
 *     grid.dijkstra(start, goal, cost_fn) → weighted shortest path
 *     grid.flood_fill(start)      → connected region from start
 */
public class PuzzGrid implements Iterable<Object> {

    private final List<List<Object>> data;
    private final int width;
    private final int height;

    // ── Construction ─────────────────────────────────────────────────────────

    public PuzzGrid(List<List<Object>> data) {
        this.data = data;
        this.height = data.size();
        this.width = height > 0 ? data.getFirst().size() : 0;
        
        // Validate rectangular grid
        for (List<Object> row : data) {
            if (row.size() != width) {
                throw new PuzzLangException("Grid rows must have equal length");
            }
        }
    }

    /**
     * Parse a string into a character grid.
     */
    public static PuzzGrid fromString(String input) {
        return fromString(input, null);
    }

    /**
     * Parse a string into a grid with optional mapper function.
     * 
     * @param input Multi-line string
     * @param mapper Optional function to transform each character (null = keep as char)
     */
    public static PuzzGrid fromString(String input, Function<String, Object> mapper) {
        String[] lines = input.split("\n", -1);
        List<List<Object>> grid = new ArrayList<>();
        
        for (String line : lines) {
            if (line.isEmpty() && grid.isEmpty()) continue; // Skip leading empty lines
            
            List<Object> row = new ArrayList<>();
            for (char c : line.toCharArray()) {
                Object value = mapper != null ? mapper.apply(String.valueOf(c)) : String.valueOf(c);
                row.add(value);
            }
            grid.add(row);
        }
        
        // Remove trailing empty rows
        while (!grid.isEmpty() && grid.getLast().isEmpty()) {
            grid.removeLast();
        }
        
        // Ensure all rows have same length (pad shorter rows if needed)
        int maxWidth = 0;
        for (List<Object> row : grid) {
            maxWidth = Math.max(maxWidth, row.size());
        }
        for (List<Object> row : grid) {
            while (row.size() < maxWidth) {
                row.add(" ");
            }
        }
        
        return new PuzzGrid(grid);
    }

    // ── Dimensions ───────────────────────────────────────────────────────────

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public PuzzList bounds() {
        PuzzList result = new PuzzList();
        result.add(width);
        result.add(height);
        return result;
    }

    public boolean isEmpty() {
        return width == 0 || height == 0;
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    // ── Access ───────────────────────────────────────────────────────────────

    /**
     * Get value at (x, y) with bounds checking.
     */
    public Object at(int x, int y) {
        if (!inBounds(x, y)) {
            throw new PuzzLangException("Grid index out of bounds: (%d, %d) not in %dx%d grid", 
                    x, y, width, height);
        }
        return data.get(y).get(x);
    }

    /**
     * Get value at (x, y) or default if out of bounds.
     */
    public Object get(int x, int y, Object defaultValue) {
        if (!inBounds(x, y)) {
            return defaultValue;
        }
        return data.get(y).get(x);
    }

    /**
     * Set value at (x, y). Returns the grid for chaining.
     */
    public PuzzGrid set(int x, int y, Object value) {
        if (!inBounds(x, y)) {
            throw new PuzzLangException("Grid index out of bounds: (%d, %d) not in %dx%d grid", 
                    x, y, width, height);
        }
        data.get(y).set(x, value);
        return this;
    }

    /**
     * Get entire row as a list.
     */
    public PuzzList row(int y) {
        if (y < 0 || y >= height) {
            throw new PuzzLangException("Row index out of bounds: %d not in 0..%d", y, height - 1);
        }
        return new PuzzList(data.get(y));
    }

    /**
     * Get entire column as a list.
     */
    public PuzzList col(int x) {
        if (x < 0 || x >= width) {
            throw new PuzzLangException("Column index out of bounds: %d not in 0..%d", x, width - 1);
        }
        PuzzList result = new PuzzList();
        for (List<Object> row : data) {
            result.add(row.get(x));
        }
        return result;
    }

    // ── Iteration ────────────────────────────────────────────────────────────

    /**
     * Returns iterable of (x, y, value) tuples for all cells.
     */
    public PuzzList cells() {
        PuzzList result = new PuzzList();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                PuzzList cell = new PuzzList();
                cell.add(x);
                cell.add(y);
                cell.add(data.get(y).get(x));
                result.add(cell);
            }
        }
        return result;
    }

    /**
     * Returns iterable of (x, y) coordinate tuples.
     */
    public PuzzList coords() {
        PuzzList result = new PuzzList();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                PuzzList coord = new PuzzList();
                coord.add(x);
                coord.add(y);
                result.add(coord);
            }
        }
        return result;
    }

    /**
     * 4-directional neighbors (N, E, S, W).
     */
    public PuzzList neighbors(int x, int y) {
        int[][] dirs = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}}; // N, E, S, W
        return getNeighbors(x, y, dirs);
    }

    /**
     * 8-directional neighbors (including diagonals).
     */
    public PuzzList neighbors8(int x, int y) {
        int[][] dirs = {
            {0, -1}, {1, -1}, {1, 0}, {1, 1},
            {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}
        };
        return getNeighbors(x, y, dirs);
    }

    private PuzzList getNeighbors(int x, int y, int[][] dirs) {
        PuzzList result = new PuzzList();
        for (int[] dir : dirs) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (inBounds(nx, ny)) {
                PuzzList coord = new PuzzList();
                coord.add(nx);
                coord.add(ny);
                result.add(coord);
            }
        }
        return result;
    }

    /**
     * Default iterator iterates over rows.
     */
    @Override
    public Iterator<Object> iterator() {
        return new Iterator<>() {
            private int y = 0;
            @Override public boolean hasNext() { return y < height; }
            @Override public Object next() {
                if (!hasNext()) throw new NoSuchElementException();
                return new PuzzList(data.get(y++));
            }
        };
    }

    // ── Finding ──────────────────────────────────────────────────────────────

    /**
     * Find first position of value, or null if not found.
     */
    public PuzzList find(Object value) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (Objects.equals(data.get(y).get(x), value)) {
                    PuzzList coord = new PuzzList();
                    coord.add(x);
                    coord.add(y);
                    return coord;
                }
            }
        }
        return null;
    }

    /**
     * Find all positions of value.
     */
    public PuzzList findAll(Object value) {
        PuzzList result = new PuzzList();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (Objects.equals(data.get(y).get(x), value)) {
                    PuzzList coord = new PuzzList();
                    coord.add(x);
                    coord.add(y);
                    result.add(coord);
                }
            }
        }
        return result;
    }

    /**
     * Count occurrences of value.
     */
    public int count(Object value) {
        int count = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (Objects.equals(data.get(y).get(x), value)) {
                    count++;
                }
            }
        }
        return count;
    }

    // ── Transformations ──────────────────────────────────────────────────────

    /**
     * Rotate 90° clockwise.
     */
    public PuzzGrid rotateCw() {
        List<List<Object>> result = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            List<Object> newRow = new ArrayList<>();
            for (int y = height - 1; y >= 0; y--) {
                newRow.add(data.get(y).get(x));
            }
            result.add(newRow);
        }
        return new PuzzGrid(result);
    }

    /**
     * Rotate 90° counter-clockwise.
     */
    public PuzzGrid rotateCcw() {
        List<List<Object>> result = new ArrayList<>();
        for (int x = width - 1; x >= 0; x--) {
            List<Object> newRow = new ArrayList<>();
            for (int y = 0; y < height; y++) {
                newRow.add(data.get(y).get(x));
            }
            result.add(newRow);
        }
        return new PuzzGrid(result);
    }

    /**
     * Flip horizontally (mirror left-right).
     */
    public PuzzGrid flipH() {
        List<List<Object>> result = new ArrayList<>();
        for (List<Object> row : data) {
            List<Object> newRow = new ArrayList<>(row);
            Collections.reverse(newRow);
            result.add(newRow);
        }
        return new PuzzGrid(result);
    }

    /**
     * Flip vertically (mirror top-bottom).
     */
    public PuzzGrid flipV() {
        List<List<Object>> result = new ArrayList<>(data);
        Collections.reverse(result);
        // Deep copy to avoid mutation
        List<List<Object>> copy = new ArrayList<>();
        for (List<Object> row : result) {
            copy.add(new ArrayList<>(row));
        }
        return new PuzzGrid(copy);
    }

    /**
     * Transpose (swap rows and columns).
     */
    public PuzzGrid transpose() {
        List<List<Object>> result = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            List<Object> newRow = new ArrayList<>();
            for (int y = 0; y < height; y++) {
                newRow.add(data.get(y).get(x));
            }
            result.add(newRow);
        }
        return new PuzzGrid(result);
    }

    /**
     * Extract rectangular subgrid (inclusive bounds).
     */
    public PuzzGrid subgrid(int x1, int y1, int x2, int y2) {
        if (x1 > x2 || y1 > y2) {
            throw new PuzzLangException("Invalid subgrid bounds: (%d,%d) to (%d,%d)", x1, y1, x2, y2);
        }
        x1 = Math.max(0, x1);
        y1 = Math.max(0, y1);
        x2 = Math.min(width - 1, x2);
        y2 = Math.min(height - 1, y2);
        
        List<List<Object>> result = new ArrayList<>();
        for (int y = y1; y <= y2; y++) {
            List<Object> newRow = new ArrayList<>();
            for (int x = x1; x <= x2; x++) {
                newRow.add(data.get(y).get(x));
            }
            result.add(newRow);
        }
        return new PuzzGrid(result);
    }

    /**
     * Create a deep copy of the grid.
     */
    public PuzzGrid copy() {
        List<List<Object>> result = new ArrayList<>();
        for (List<Object> row : data) {
            result.add(new ArrayList<>(row));
        }
        return new PuzzGrid(result);
    }

    // ── Pathfinding ──────────────────────────────────────────────────────────

    /**
     * BFS shortest path from start to goal.
     * Returns list of (x,y) coordinates forming the path, or empty list if no path.
     */
    public PuzzList bfs(PuzzList start, PuzzList goal) {
        return bfs(start, goal, null);
    }

    /**
     * BFS shortest path with walkable constraint.
     * @param walkable String of characters that can be walked on (null = all cells walkable)
     */
    public PuzzList bfs(PuzzList start, PuzzList goal, String walkable) {
        int sx = PuzzRuntime.toInt(start.get(0));
        int sy = PuzzRuntime.toInt(start.get(1));
        int gx = PuzzRuntime.toInt(goal.get(0));
        int gy = PuzzRuntime.toInt(goal.get(1));

        if (!inBounds(sx, sy) || !inBounds(gx, gy)) {
            return new PuzzList();
        }

        Set<String> walkableSet = walkable != null ? 
            new HashSet<>(Arrays.asList(walkable.split(""))) : null;

        // BFS
        Queue<int[]> queue = new LinkedList<>();
        Map<String, int[]> cameFrom = new HashMap<>();
        
        queue.offer(new int[]{sx, sy});
        cameFrom.put(sx + "," + sy, null);

        int[][] dirs = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int cx = current[0];
            int cy = current[1];

            if (cx == gx && cy == gy) {
                // Reconstruct path
                return reconstructPath(cameFrom, gx, gy);
            }

            for (int[] dir : dirs) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];
                String key = nx + "," + ny;

                if (inBounds(nx, ny) && !cameFrom.containsKey(key)) {
                    String cellValue = String.valueOf(data.get(ny).get(nx));
                    if (walkableSet == null || walkableSet.contains(cellValue)) {
                        cameFrom.put(key, new int[]{cx, cy});
                        queue.offer(new int[]{nx, ny});
                    }
                }
            }
        }

        return new PuzzList(); // No path found
    }

    private PuzzList reconstructPath(Map<String, int[]> cameFrom, int gx, int gy) {
        List<PuzzList> path = new ArrayList<>();
        int[] current = new int[]{gx, gy};
        
        while (current != null) {
            PuzzList coord = new PuzzList();
            coord.add(current[0]);
            coord.add(current[1]);
            path.addFirst(coord);
            current = cameFrom.get(current[0] + "," + current[1]);
        }
        
        PuzzList result = new PuzzList();
        for (PuzzList p : path) {
            result.add(p);
        }
        return result;
    }

    /**
     * Dijkstra's algorithm for weighted shortest path.
     * Cost function takes (x, y) and returns the cost to enter that cell.
     */
    public PuzzList dijkstra(PuzzList start, PuzzList goal, Function<int[], Integer> costFn) {
        int sx = PuzzRuntime.toInt(start.get(0));
        int sy = PuzzRuntime.toInt(start.get(1));
        int gx = PuzzRuntime.toInt(goal.get(0));
        int gy = PuzzRuntime.toInt(goal.get(1));

        if (!inBounds(sx, sy) || !inBounds(gx, gy)) {
            return new PuzzList();
        }

        // Priority queue: {cost, x, y}
        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a[0]));
        Map<String, Long> dist = new HashMap<>();
        Map<String, int[]> cameFrom = new HashMap<>();

        pq.offer(new long[]{0, sx, sy});
        dist.put(sx + "," + sy, 0L);
        cameFrom.put(sx + "," + sy, null);

        int[][] dirs = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

        while (!pq.isEmpty()) {
            long[] current = pq.poll();
            long currentDist = current[0];
            int cx = (int) current[1];
            int cy = (int) current[2];

            if (cx == gx && cy == gy) {
                return reconstructPath(cameFrom, gx, gy);
            }

            String currentKey = cx + "," + cy;
            if (currentDist > dist.getOrDefault(currentKey, Long.MAX_VALUE)) {
                continue;
            }

            for (int[] dir : dirs) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];
                
                if (!inBounds(nx, ny)) continue;

                int cost = costFn != null ? costFn.apply(new int[]{nx, ny}) : 1;
                if (cost < 0) continue; // Negative cost means impassable

                long newDist = currentDist + cost;
                String key = nx + "," + ny;

                if (newDist < dist.getOrDefault(key, Long.MAX_VALUE)) {
                    dist.put(key, newDist);
                    cameFrom.put(key, new int[]{cx, cy});
                    pq.offer(new long[]{newDist, nx, ny});
                }
            }
        }

        return new PuzzList(); // No path found
    }

    /**
     * Flood fill from start position.
     * Returns list of all connected coordinates with the same value as start.
     */
    public PuzzList floodFill(PuzzList start) {
        int sx = PuzzRuntime.toInt(start.get(0));
        int sy = PuzzRuntime.toInt(start.get(1));

        if (!inBounds(sx, sy)) {
            return new PuzzList();
        }

        Object targetValue = data.get(sy).get(sx);
        Set<String> visited = new HashSet<>();
        PuzzList result = new PuzzList();
        Queue<int[]> queue = new LinkedList<>();

        queue.offer(new int[]{sx, sy});
        visited.add(sx + "," + sy);

        int[][] dirs = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int cx = current[0];
            int cy = current[1];

            PuzzList coord = new PuzzList();
            coord.add(cx);
            coord.add(cy);
            result.add(coord);

            for (int[] dir : dirs) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];
                String key = nx + "," + ny;

                if (inBounds(nx, ny) && !visited.contains(key)) {
                    if (Objects.equals(data.get(ny).get(nx), targetValue)) {
                        visited.add(key);
                        queue.offer(new int[]{nx, ny});
                    }
                }
            }
        }

        return result;
    }

    // ── Display ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                sb.append(data.get(y).get(x));
            }
            if (y < height - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Pretty print with coordinates.
     */
    public String toPrettyString() {
        StringBuilder sb = new StringBuilder();
        
        // Column headers
        sb.append("   ");
        for (int x = 0; x < width; x++) {
            sb.append(x % 10);
        }
        sb.append("\n");
        
        for (int y = 0; y < height; y++) {
            sb.append(String.format("%2d ", y));
            for (int x = 0; x < width; x++) {
                sb.append(data.get(y).get(x));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PuzzGrid other)) return false;
        return data.equals(other.data);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    // ── Internal access for testing ──────────────────────────────────────────

    List<List<Object>> getData() {
        return data;
    }
}
