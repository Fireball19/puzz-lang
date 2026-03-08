package com.puzzlang.runtime.builtins;

import com.puzzlang.runtime.*;

/**
 * Registers grid-related methods with the MethodRegistry.
 *
 * Grid methods fall into several categories:
 *   - Dimensions: width, height, bounds
 *   - Access: at, get, row, col
 *   - Iteration: cells, coords, neighbors, neighbors8
 *   - Finding: find, find_all, count
 *   - Transformations: rotate_cw, rotate_ccw, flip_h, flip_v, transpose, subgrid, copy
 *   - Pathfinding: bfs, dijkstra, flood_fill
 */
public class GridBuiltins {

    private GridBuiltins() {} // Static utility class

    /**
     * Register all grid methods with the given registry.
     */
    public static void register(MethodRegistry registry) {
        Class<PuzzGrid> type = PuzzGrid.class;

        // ── Dimensions ───────────────────────────────────────────────────
        registry.register(type, "width", (r, args) -> ((PuzzGrid) r).width());
        registry.register(type, "height", (r, args) -> ((PuzzGrid) r).height());
        registry.register(type, "bounds", (r, args) -> ((PuzzGrid) r).bounds());
        registry.register(type, "isEmpty", (r, args) -> ((PuzzGrid) r).isEmpty());
        registry.register(type, "is_empty", (r, args) -> ((PuzzGrid) r).isEmpty());

        // ── Access ───────────────────────────────────────────────────────
        registry.register(type, "at", (r, args) -> {
            requireArgs("at", args, 2);
            return ((PuzzGrid) r).at(PuzzRuntime.toInt(args[0]), PuzzRuntime.toInt(args[1]));
        });

        registry.register(type, "get", (r, args) -> {
            if (args.length < 2 || args.length > 3) {
                throw new PuzzLangException("method 'get' expects 2 or 3 arguments, got %d", args.length);
            }
            int x = PuzzRuntime.toInt(args[0]);
            int y = PuzzRuntime.toInt(args[1]);
            Object defaultVal = args.length == 3 ? args[2] : null;
            return ((PuzzGrid) r).get(x, y, defaultVal);
        });

        registry.register(type, "set", (r, args) -> {
            requireArgs("set", args, 3);
            int x = PuzzRuntime.toInt(args[0]);
            int y = PuzzRuntime.toInt(args[1]);
            return ((PuzzGrid) r).set(x, y, args[2]);
        });

        registry.register(type, "row", (r, args) -> {
            requireArgs("row", args, 1);
            return ((PuzzGrid) r).row(PuzzRuntime.toInt(args[0]));
        });

        registry.register(type, "col", (r, args) -> {
            requireArgs("col", args, 1);
            return ((PuzzGrid) r).col(PuzzRuntime.toInt(args[0]));
        });

        registry.register(type, "inBounds", (r, args) -> {
            requireArgs("inBounds", args, 2);
            return ((PuzzGrid) r).inBounds(PuzzRuntime.toInt(args[0]), PuzzRuntime.toInt(args[1]));
        });

        registry.register(type, "in_bounds", (r, args) -> {
            requireArgs("in_bounds", args, 2);
            return ((PuzzGrid) r).inBounds(PuzzRuntime.toInt(args[0]), PuzzRuntime.toInt(args[1]));
        });

        // ── Iteration ────────────────────────────────────────────────────
        registry.register(type, "cells", (r, args) -> ((PuzzGrid) r).cells());
        registry.register(type, "coords", (r, args) -> ((PuzzGrid) r).coords());

        registry.register(type, "neighbors", (r, args) -> {
            requireArgs("neighbors", args, 2);
            return ((PuzzGrid) r).neighbors(PuzzRuntime.toInt(args[0]), PuzzRuntime.toInt(args[1]));
        });

        registry.register(type, "neighbors8", (r, args) -> {
            requireArgs("neighbors8", args, 2);
            return ((PuzzGrid) r).neighbors8(PuzzRuntime.toInt(args[0]), PuzzRuntime.toInt(args[1]));
        });

        // ── Finding ──────────────────────────────────────────────────────
        registry.register(type, "find", (r, args) -> {
            requireArgs("find", args, 1);
            PuzzList result = ((PuzzGrid) r).find(args[0]);
            return result != null ? result : "null";  // Return string "null" for display
        });

        registry.register(type, "find_all", (r, args) -> {
            requireArgs("find_all", args, 1);
            return ((PuzzGrid) r).findAll(args[0]);
        });

        registry.register(type, "findAll", (r, args) -> {
            requireArgs("findAll", args, 1);
            return ((PuzzGrid) r).findAll(args[0]);
        });

        registry.register(type, "count", (r, args) -> {
            requireArgs("count", args, 1);
            return ((PuzzGrid) r).count(args[0]);
        });

        // ── Transformations ──────────────────────────────────────────────
        registry.register(type, "rotate_cw", (r, args) -> ((PuzzGrid) r).rotateCw());
        registry.register(type, "rotateCw", (r, args) -> ((PuzzGrid) r).rotateCw());
        
        registry.register(type, "rotate_ccw", (r, args) -> ((PuzzGrid) r).rotateCcw());
        registry.register(type, "rotateCcw", (r, args) -> ((PuzzGrid) r).rotateCcw());
        
        registry.register(type, "flip_h", (r, args) -> ((PuzzGrid) r).flipH());
        registry.register(type, "flipH", (r, args) -> ((PuzzGrid) r).flipH());
        
        registry.register(type, "flip_v", (r, args) -> ((PuzzGrid) r).flipV());
        registry.register(type, "flipV", (r, args) -> ((PuzzGrid) r).flipV());
        
        registry.register(type, "transpose", (r, args) -> ((PuzzGrid) r).transpose());
        
        registry.register(type, "subgrid", (r, args) -> {
            requireArgs("subgrid", args, 4);
            return ((PuzzGrid) r).subgrid(
                PuzzRuntime.toInt(args[0]),
                PuzzRuntime.toInt(args[1]),
                PuzzRuntime.toInt(args[2]),
                PuzzRuntime.toInt(args[3])
            );
        });

        registry.register(type, "copy", (r, args) -> ((PuzzGrid) r).copy());

        // ── Pathfinding ──────────────────────────────────────────────────
        registry.register(type, "bfs", (r, args) -> {
            if (args.length < 2 || args.length > 3) {
                throw new PuzzLangException("method 'bfs' expects 2 or 3 arguments, got %d", args.length);
            }
            if (!(args[0] instanceof PuzzList start)) {
                throw new PuzzLangException("bfs() start must be a coordinate tuple");
            }
            if (!(args[1] instanceof PuzzList goal)) {
                throw new PuzzLangException("bfs() goal must be a coordinate tuple");
            }
            String walkable = args.length == 3 ? PuzzRuntime.toString(args[2]) : null;
            return ((PuzzGrid) r).bfs(start, goal, walkable);
        });

        registry.register(type, "flood_fill", (r, args) -> {
            requireArgs("flood_fill", args, 1);
            if (!(args[0] instanceof PuzzList start)) {
                throw new PuzzLangException("flood_fill() start must be a coordinate tuple");
            }
            return ((PuzzGrid) r).floodFill(start);
        });

        registry.register(type, "floodFill", (r, args) -> {
            requireArgs("floodFill", args, 1);
            if (!(args[0] instanceof PuzzList start)) {
                throw new PuzzLangException("floodFill() start must be a coordinate tuple");
            }
            return ((PuzzGrid) r).floodFill(start);
        });

        // Note: dijkstra with cost_fn requires lambda support
        // For now, we provide a simpler version that uses cell values as costs
        registry.register(type, "dijkstra", (r, args) -> {
            if (args.length < 2 || args.length > 3) {
                throw new PuzzLangException("method 'dijkstra' expects 2 or 3 arguments, got %d", args.length);
            }
            if (!(args[0] instanceof PuzzList start)) {
                throw new PuzzLangException("dijkstra() start must be a coordinate tuple");
            }
            if (!(args[1] instanceof PuzzList goal)) {
                throw new PuzzLangException("dijkstra() goal must be a coordinate tuple");
            }
            
            // If third arg provided, it's treated as a walkable string (impassable = '#')
            // Otherwise use numeric cell values as costs
            PuzzGrid grid = (PuzzGrid) r;
            if (args.length == 3) {
                String walkable = PuzzRuntime.toString(args[2]);
                return grid.dijkstra(start, goal, coord -> {
                    Object val = grid.get(coord[0], coord[1], null);
                    String s = String.valueOf(val);
                    return walkable.contains(s) ? 1 : -1; // -1 means impassable
                });
            } else {
                // Use numeric cell values as costs
                return grid.dijkstra(start, goal, coord -> {
                    Object val = grid.get(coord[0], coord[1], null);
                    if (val instanceof Number n) {
                        return n.intValue();
                    }
                    // Try parsing as int
                    try {
                        return Integer.parseInt(String.valueOf(val));
                    } catch (NumberFormatException e) {
                        return 1; // Default cost
                    }
                });
            }
        });
    }

    // ── Helper ──────────────────────────────────────────────────────────────

    private static void requireArgs(String method, Object[] args, int expected) {
        if (args.length != expected) {
            throw PuzzLangException.argumentCount(method, expected, args.length);
        }
    }
}
