grammar PuzzLang;

// Inject package declaration into every generated Java file
@header {
package com.puzzlang.parser;
}

// ─────────────────────────────────────────
//  Parser Rules
// ─────────────────────────────────────────

program
    : statement+ EOF
    ;

statement
    : varDecl
    | printStmt
    | ifStmt
    | whileStmt
    | forInStmt
    | matchStmt
    | exprStmt
    ;

// Variable declaration with optional destructuring:
//   let x = 5
//   let (x, y) = point
//   let a, b = b, a           (multiple assignment)
//   let x, y, z = tuple
varDecl
    : 'let' destructureTarget '=' expr NEWLINE
    ;

// Destructuring target for let statements
destructureTarget
    : ID                                              # SingleVar
    | '(' ID (',' ID)+ ')'                           # TupleDestructure
    | ID (',' ID)+                                   # MultipleAssign
    ;

printStmt  : 'print' expr NEWLINE ;

ifStmt
    : 'if' expr ':' NEWLINE
      INDENT NEWLINE? statement+ DEDENT NEWLINE?
      ('else' ':' NEWLINE INDENT NEWLINE? statement+ DEDENT NEWLINE?)?
    ;

whileStmt
    : 'while' expr ':' NEWLINE
      INDENT NEWLINE? statement+ DEDENT NEWLINE?
    ;

// for x in range: — iterates over any iterable (ranges, lists)
// for (x, y) in points: — tuple destructuring in iteration
forInStmt
    : 'for' forTarget 'in' expr ':' NEWLINE
      INDENT NEWLINE? statement+ DEDENT NEWLINE?
    ;

// Target for for-in loops (single var or tuple destructure)
forTarget
    : ID                                              # ForSingleVar
    | '(' ID (',' ID)+ ')'                           # ForTupleDestructure
    ;

// match expr:
//     "pattern {x}" -> statement
//     (0, 0) -> statement
//     (x, 0) -> statement
//     _ -> statement
matchStmt
    : 'match' expr ':' NEWLINE
      INDENT NEWLINE? matchArm+ DEDENT NEWLINE?
    ;

matchArm
    : matchPattern '->' statement
    ;

matchPattern
    : STRING                                          # PatternString   // "move {n} from {a} to {b}"
    | '(' tuplePatternElement (',' tuplePatternElement)+ ')'  # PatternTuple   // (0, 0), (x, y), (x, 0)
    | '_'                                             # PatternWildcard // default/catch-all
    ;

// Elements in a tuple pattern: either a literal, variable binding, or wildcard
tuplePatternElement
    : INT                                             # TuplePatternInt
    | STRING                                          # TuplePatternString  
    | '_'                                             # TuplePatternWildcard
    | ID                                              # TuplePatternVar     // captures to variable
    ;

exprStmt : expr NEWLINE ;

expr
    : expr '.' method=ID
        '(' (expr (',' expr)*)? ')'          # MethodCall     // r.sum() - highest precedence
    | expr op=('*' | '/' | '%')      expr   # MulDivMod
    | expr op=('+' | '-')            expr   # AddSub
    | expr op=('==' | '!=' | '<' | '<=' | '>' | '>=') expr  # Compare
    | expr '..' expr                         # RangeLit       // 1..10
    | expr '..=' expr                        # RangeInclusive // 1..=10
    | '(' expr ')'                           # Parens
    | '(' expr (',' expr)+ ')'               # TupleLit       // (1, 2), (a, b, c)
    | '[' expr 'for' forTarget 'in' expr ('if' expr)? ']'  # ListComprehension
    | '[' expr 'for' forTarget 'in' expr 'for' forTarget 'in' expr ']'  # NestedListComprehension
    | '[' (expr (',' expr)*)? ']'            # ListLit        // [], [1, 2, 3]
    | 'stdin' '(' ')'                        # StdinCall      // stdin()
    | 'read' '(' expr ')'                    # ReadCall       // read("file.txt")
    | ID '(' (expr (',' expr)*)? ')'         # FunctionCall   // gcd(a, b), primes_up_to(n)
    | INT                                    # IntLit
    | STRING                                 # StringLit
    | BOOL                                   # BoolLit
    | ID                                     # Var
    ;

// ─────────────────────────────────────────
//  Lexer Rules
// ─────────────────────────────────────────

// Keywords and special tokens MUST come before ID
INDENT  : 'INDENT' ;
DEDENT  : 'DEDENT' ;
BOOL    : 'true' | 'false' ;

INT     : [0-9]+ ;
STRING  : '"' (~["\r\n])* '"' ;
ID      : [a-zA-Z_][a-zA-Z0-9_]* ;

NEWLINE : [\r\n]+ ;

WS      : [ \t]+    -> skip ;
COMMENT : '#' ~[\r\n]* -> skip ;
