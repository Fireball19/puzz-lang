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
      INDENT NEWLINE? statement+ DEDENT NEWLINE?
      ('else' ':' NEWLINE INDENT NEWLINE? statement+ DEDENT NEWLINE?)?
    ;

whileStmt
    : 'while' expr ':' NEWLINE
      INDENT NEWLINE? statement+ DEDENT NEWLINE?
    ;

// for x in range: — iterates over any iterable (ranges, lists)
forInStmt
    : 'for' ID 'in' expr ':' NEWLINE
      INDENT NEWLINE? statement+ DEDENT NEWLINE?
    ;

exprStmt : expr NEWLINE ;

expr
    : expr '.' method=ID
        '(' (expr (',' expr)*)? ')'          # MethodCall     // r.sum() - highest precedence
    | expr op=('*' | '/')            expr   # MulDiv
    | expr op=('+' | '-')            expr   # AddSub
    | expr op=('==' | '!=' | '<' | '<=' | '>' | '>=') expr  # Compare
    | expr '..' expr                         # RangeLit       // 1..10
    | expr '..=' expr                        # RangeInclusive // 1..=10
    | '(' expr ')'                           # Parens
    | 'stdin' '(' ')'                        # StdinCall      // stdin()
    | 'read' '(' expr ')'                    # ReadCall       // read("file.txt")
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