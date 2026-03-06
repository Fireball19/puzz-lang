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
    | exprStmt
    ;

varDecl    : 'let' ID '=' expr NEWLINE ;
printStmt  : 'print' expr NEWLINE ;

ifStmt
    : 'if' expr ':' NEWLINE
      INDENT statement+ DEDENT
      ('else' ':' NEWLINE INDENT statement+ DEDENT)?
    ;

whileStmt
    : 'while' expr ':' NEWLINE
      INDENT statement+ DEDENT
    ;

// for x in range: — iterates over any iterable (ranges, later lists)
forInStmt
    : 'for' ID 'in' expr ':' NEWLINE
      INDENT statement+ DEDENT
    ;

exprStmt : expr NEWLINE ;

expr
    : expr op=('*' | '/')            expr   # MulDiv
    | expr op=('+' | '-')            expr   # AddSub
    | expr op=('==' | '!=' | '<' | '<=' | '>' | '>=') expr  # Compare
    | expr '..' expr                         # RangeLit       // 1..10
    | expr '..=' expr                        # RangeInclusive // 1..=10
    | expr '.' method=ID
        '(' (expr (',' expr)*)? ')'          # MethodCall     // r.sum()
    | '(' expr ')'                           # Parens
    | INT                                    # IntLit
    | STRING                                 # StringLit
    | BOOL                                   # BoolLit
    | ID                                     # Var
    ;

// ─────────────────────────────────────────
//  Lexer Rules
// ─────────────────────────────────────────

BOOL    : 'true' | 'false' ;
INT     : [0-9]+ ;
STRING  : '"' (~["\r\n])* '"' ;
ID      : [a-zA-Z_][a-zA-Z0-9_]* ;

INDENT  : 'INDENT' ;
DEDENT  : 'DEDENT' ;
NEWLINE : [\r\n]+ ;

WS      : [ \t]+    -> skip ;
COMMENT : '#' ~[\r\n]* -> skip ;