package com.puzzlang.intellij;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

/**
 * Syntax highlighter for PuzzLang.
 * Maps token types to text attributes for colorization.
 */
public class PuzzSyntaxHighlighter extends SyntaxHighlighterBase {

    // Text attribute keys
    public static final TextAttributesKey KEYWORD =
            createTextAttributesKey("PUZZ_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey STRING =
            createTextAttributesKey("PUZZ_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey NUMBER =
            createTextAttributesKey("PUZZ_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey COMMENT =
            createTextAttributesKey("PUZZ_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey OPERATOR =
            createTextAttributesKey("PUZZ_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey BRACES =
            createTextAttributesKey("PUZZ_BRACES", DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey BRACKETS =
            createTextAttributesKey("PUZZ_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey PARENTHESES =
            createTextAttributesKey("PUZZ_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey IDENTIFIER =
            createTextAttributesKey("PUZZ_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey BAD_CHARACTER =
            createTextAttributesKey("PUZZ_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);

    private static final TextAttributesKey[] KEYWORD_KEYS = new TextAttributesKey[]{KEYWORD};
    private static final TextAttributesKey[] STRING_KEYS = new TextAttributesKey[]{STRING};
    private static final TextAttributesKey[] NUMBER_KEYS = new TextAttributesKey[]{NUMBER};
    private static final TextAttributesKey[] COMMENT_KEYS = new TextAttributesKey[]{COMMENT};
    private static final TextAttributesKey[] OPERATOR_KEYS = new TextAttributesKey[]{OPERATOR};
    private static final TextAttributesKey[] BRACE_KEYS = new TextAttributesKey[]{BRACES};
    private static final TextAttributesKey[] BRACKET_KEYS = new TextAttributesKey[]{BRACKETS};
    private static final TextAttributesKey[] PAREN_KEYS = new TextAttributesKey[]{PARENTHESES};
    private static final TextAttributesKey[] IDENTIFIER_KEYS = new TextAttributesKey[]{IDENTIFIER};
    private static final TextAttributesKey[] BAD_CHAR_KEYS = new TextAttributesKey[]{BAD_CHARACTER};
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new PuzzLexer();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        // Keywords
        if (PuzzTokenTypes.KEYWORDS.contains(tokenType)) {
            return KEYWORD_KEYS;
        }

        // String
        if (tokenType.equals(PuzzTokenTypes.STRING)) {
            return STRING_KEYS;
        }

        // Number
        if (tokenType.equals(PuzzTokenTypes.NUMBER)) {
            return NUMBER_KEYS;
        }

        // Comment
        if (PuzzTokenTypes.COMMENTS.contains(tokenType)) {
            return COMMENT_KEYS;
        }

        // Operators
        if (PuzzTokenTypes.OPERATORS.contains(tokenType)) {
            return OPERATOR_KEYS;
        }

        // Braces, brackets, parentheses
        if (tokenType.equals(PuzzTokenTypes.LBRACE) || tokenType.equals(PuzzTokenTypes.RBRACE)) {
            return BRACE_KEYS;
        }
        if (tokenType.equals(PuzzTokenTypes.LBRACKET) || tokenType.equals(PuzzTokenTypes.RBRACKET)) {
            return BRACKET_KEYS;
        }
        if (tokenType.equals(PuzzTokenTypes.LPAREN) || tokenType.equals(PuzzTokenTypes.RPAREN)) {
            return PAREN_KEYS;
        }

        // Identifier
        if (tokenType.equals(PuzzTokenTypes.IDENTIFIER)) {
            return IDENTIFIER_KEYS;
        }

        // Bad character
        if (tokenType.equals(PuzzTokenTypes.BAD_CHARACTER) || tokenType.equals(TokenType.BAD_CHARACTER)) {
            return BAD_CHAR_KEYS;
        }

        return EMPTY_KEYS;
    }
}
