# PuzzLang

<div align="left">
  
  [![License badge](https://img.shields.io/github/license/Fireball19/puzz-lang)](https://github.com/Fireball19/puzz-lang/blob/main/LICENSE)
  [![GitHub release badge](https://badgen.net/github/release/Fireball19/puzz-lang/stable)](https://github.com/Fireball19/puzz-lang/releases/latest)
  [![Java CI with Gradle badge](https://github.com/Fireball19/puzz-lang/actions/workflows/gradle.yml/badge.svg?branch=develop)](https://github.com/Fireball19/puzz-lang/actions/workflows/gradle.yml)
  
</div>

<img align="left" width="128" height="128" src="./intellij-plugin/src/main/resources/icons/puzz-file.svg"/>

A domain-specific language for solving coding puzzles and challenges like Advent of Code.

<br clear="left"/>

## Quick Example

```python
# Sum numbers from parsed input
let total = 0
for line in stdin().lines():
    match line:
        "add {n}" -> let total = total + n
        _ -> print "unknown"
print total
```

## Language Features

### Input Handling
- `stdin()` — read all input from stdin
- `read("file.txt")` — read file contents

### String Methods
- `.lines()` / `.paragraphs()` — split by newlines or blank lines
- `.split(sep)` / `.words()` / `.chars()` — tokenize strings
- `.trim()` / `.replace()` / `.contains()` / `.startsWith()`
- `.toInt()` / `.from_base(n)` — parse numbers

### Pattern Matching
```python
match line:
    "move {n} from {a} to {b}" -> print n + a + b
    _ -> print "no match"
```
Captures bind as variables (integers when numeric).

### Ranges
```python
let r = 1..10      # exclusive: 1 to 9
let ri = 1..=10    # inclusive: 1 to 10

r.sum()            # Gauss formula (O(1))
r.contains(5)
r.overlaps(other)
r.step(2)          # every 2nd element
```

### Lists
```python
list.sum() / .product() / .min() / .max()
list.first() / .last() / .get(i)
list.sorted() / .reversed() / .unique()
list.contains(x) / .indexOf(x)
list.join(", ")
```

### List Comprehensions
```python
# Basic comprehension
[x * x for x in 1..=10]              # [1, 4, 9, ..., 100]

# With filtering
[x for x in 1..=20 if x % 2 == 0]    # [2, 4, 6, ..., 20]

# Nested comprehension
[(x, y) for x in 1..=3 for y in 1..=3]

# With tuple destructuring
let points = [(1, 2), (3, 4), (5, 6)]
[x + y for (x, y) in points]         # [3, 7, 11]
```

### Tuples
```python
# Create tuples
let point = (3, 4)
let triple = (1, 2, 3)

# Destructuring
let (x, y) = point
let (a, b, c) = triple

# Access elements
point.get(0)       # 3
point.first()      # 3
point.last()       # 4
point.size()       # 2

# Use in comprehensions
let coords = [(0, 0), (1, 1), (2, 2)]
for (x, y) in coords:
    print x + y
```

### 2D Grids
```python
# Create from input
let grid = stdin().grid()              # parse as char grid
let grid = stdin().grid(toInt)         # parse with mapper

# Dimensions
grid.width()                           # columns
grid.height()                          # rows
grid.bounds()                          # (width, height) tuple

# Access
grid.at(x, y)                          # bounds-checked access
grid.get(x, y, default)                # with default for out-of-bounds
grid.row(y)                            # entire row as list
grid.col(x)                            # entire column as list

# Iteration
grid.cells()                           # (x, y, value) tuples
grid.coords()                          # (x, y) coordinate tuples
grid.neighbors(x, y)                   # 4-directional neighbors
grid.neighbors8(x, y)                  # 8-directional neighbors

# Finding
grid.find(val)                         # first (x, y) position
grid.find_all(val)                     # all positions
grid.count(val)                        # count occurrences

# Transformations
grid.rotate_cw()                       # rotate 90° clockwise
grid.rotate_ccw()                      # rotate 90° counter-clockwise
grid.flip_h()                          # flip horizontally
grid.flip_v()                          # flip vertically
grid.transpose()                       # swap rows and columns
grid.subgrid(x1, y1, x2, y2)           # extract region

# Pathfinding
grid.bfs(start, goal)                  # shortest path BFS
grid.dijkstra(start, goal, cost_fn)    # weighted shortest path
grid.flood_fill(start)                 # connected region
```

### Math Builtins
```python
gcd(a, b)          lcm(a, b, c)
primes_up_to(100)  is_prime(n)
nth_prime(10)      prime_factors(60)
digits(12345)      digit_sum(n)
factorial(n)       binomial(n, k)
mod_pow(b, e, m)   popcount(n)
```

### Control Flow
```python
if x > 0:
    print "positive"
else:
    print "non-positive"

while n > 0:
    let n = n - 1

for x in 1..=10:
    print x
```

## Running

```bash
./gradlew run --args="solution.puzz"
```

## Design Goals

- **Concise**: Minimize boilerplate for puzzle solving
- **Batteries included**: Common operations built-in (GCD, primes, ranges)
- **Pattern matching**: Parse structured input with `{captures}`
