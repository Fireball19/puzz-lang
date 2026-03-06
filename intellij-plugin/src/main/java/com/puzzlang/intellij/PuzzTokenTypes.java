package com.puzzlang.intellij;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * Token types for PuzzLang lexer.
 */
public class PuzzTokenTypes {

    // Keywords
    public static final IElementType LET = new PuzzTokenType("LET");
    public static final IElementType IF = new PuzzTokenType("IF");
    public static final IElementType ELSE = new PuzzTokenType("ELSE");
    public static final IElementType FOR = new PuzzTokenType("FOR");
    public static final IElementType WHILE = new PuzzTokenType("WHILE");
    public static final IElementType IN = new PuzzTokenType("IN");
    public static final IElementType MATCH = new PuzzTokenType("MATCH");
    public static final IElementType PRINT = new PuzzTokenType("PRINT");
    public static final IElementType RETURN = new PuzzTokenType("RETURN");
    public static final IElementType FN = new PuzzTokenType("FN");
    public static final IElementType TRUE = new PuzzTokenType("TRUE");
    public static final IElementType FALSE = new PuzzTokenType("FALSE");

    // Literals
    public static final IElementType NUMBER = new PuzzTokenType("NUMBER");
    public static final IElementType STRING = new PuzzTokenType("STRING");
    public static final IElementType IDENTIFIER = new PuzzTokenType("IDENTIFIER");

    // Operators
    public static final IElementType PLUS = new PuzzTokenType("PLUS");
    public static final IElementType MINUS = new PuzzTokenType("MINUS");
    public static final IElementType STAR = new PuzzTokenType("STAR");
    public static final IElementType SLASH = new PuzzTokenType("SLASH");
    public static final IElementType PERCENT = new PuzzTokenType("PERCENT");
    public static final IElementType EQUALS = new PuzzTokenType("EQUALS");
    public static final IElementType EQ = new PuzzTokenType("EQ");
    public static final IElementType NEQ = new PuzzTokenType("NEQ");
    public static final IElementType LT = new PuzzTokenType("LT");
    public static final IElementType GT = new PuzzTokenType("GT");
    public static final IElementType LE = new PuzzTokenType("LE");
    public static final IElementType GE = new PuzzTokenType("GE");
    public static final IElementType ARROW = new PuzzTokenType("ARROW");
    public static final IElementType RANGE = new PuzzTokenType("RANGE");
    public static final IElementType RANGE_INCL = new PuzzTokenType("RANGE_INCL");

    // Delimiters
    public static final IElementType LPAREN = new PuzzTokenType("LPAREN");
    public static final IElementType RPAREN = new PuzzTokenType("RPAREN");
    public static final IElementType LBRACKET = new PuzzTokenType("LBRACKET");
    public static final IElementType RBRACKET = new PuzzTokenType("RBRACKET");
    public static final IElementType LBRACE = new PuzzTokenType("LBRACE");
    public static final IElementType RBRACE = new PuzzTokenType("RBRACE");
    public static final IElementType COLON = new PuzzTokenType("COLON");
    public static final IElementType COMMA = new PuzzTokenType("COMMA");
    public static final IElementType DOT = new PuzzTokenType("DOT");

    // Comments and whitespace
    public static final IElementType COMMENT = new PuzzTokenType("COMMENT");
    public static final IElementType LINE_COMMENT = new PuzzTokenType("LINE_COMMENT");
    public static final IElementType NEWLINE = new PuzzTokenType("NEWLINE");
    public static final IElementType INDENT = new PuzzTokenType("INDENT");
    public static final IElementType DEDENT = new PuzzTokenType("DEDENT");

    // Special
    public static final IElementType BAD_CHARACTER = new PuzzTokenType("BAD_CHARACTER");

    // Token sets for highlighting
    public static final TokenSet KEYWORDS = TokenSet.create(
            LET, IF, ELSE, FOR, WHILE, IN, MATCH, PRINT, RETURN, FN, TRUE, FALSE
    );

    public static final TokenSet OPERATORS = TokenSet.create(
            PLUS, MINUS, STAR, SLASH, PERCENT, EQUALS, EQ, NEQ, LT, GT, LE, GE, ARROW, RANGE, RANGE_INCL
    );

    public static final TokenSet BRACES = TokenSet.create(
            LPAREN, RPAREN, LBRACKET, RBRACKET, LBRACE, RBRACE
    );

    public static final TokenSet COMMENTS = TokenSet.create(
            COMMENT, LINE_COMMENT
    );

    public static final TokenSet STRINGS = TokenSet.create(STRING);

    public static final TokenSet NUMBERS = TokenSet.create(NUMBER);
}
