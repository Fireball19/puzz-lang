# PuzzLang

A domain-specific language for solving coding puzzles and challenges like Advent of Code.

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