package com.puzzlang.intellij;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Lexer for PuzzLang source files.
 * Tokenizes the input for syntax highlighting.
 */
public class PuzzLexer extends LexerBase {

    private static final Map<String, IElementType> KEYWORDS = new HashMap<>();

    static {
        KEYWORDS.put("let", PuzzTokenTypes.LET);
        KEYWORDS.put("if", PuzzTokenTypes.IF);
        KEYWORDS.put("else", PuzzTokenTypes.ELSE);
        KEYWORDS.put("for", PuzzTokenTypes.FOR);
        KEYWORDS.put("while", PuzzTokenTypes.WHILE);
        KEYWORDS.put("in", PuzzTokenTypes.IN);
        KEYWORDS.put("match", PuzzTokenTypes.MATCH);
        KEYWORDS.put("print", PuzzTokenTypes.PRINT);
        KEYWORDS.put("return", PuzzTokenTypes.RETURN);
        KEYWORDS.put("fn", PuzzTokenTypes.FN);
        KEYWORDS.put("true", PuzzTokenTypes.TRUE);
        KEYWORDS.put("false", PuzzTokenTypes.FALSE);
    }

    private CharSequence buffer;
    private int bufferEnd;
    private int tokenStart;
    private int tokenEnd;
    private IElementType tokenType;

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.bufferEnd = endOffset;
        this.tokenStart = startOffset;
        this.tokenEnd = startOffset;
        this.tokenType = null;
        advance();
    }

    @Override
    public int getState() {
        return 0;
    }

    @Nullable
    @Override
    public IElementType getTokenType() {
        return tokenType;
    }

    @Override
    public int getTokenStart() {
        return tokenStart;
    }

    @Override
    public int getTokenEnd() {
        return tokenEnd;
    }

    @Override
    public void advance() {
        tokenStart = tokenEnd;
        if (tokenStart >= bufferEnd) {
            tokenType = null;
            return;
        }

        char c = buffer.charAt(tokenStart);

        // Comment
        if (c == '#') {
            tokenEnd = tokenStart + 1;
            while (tokenEnd < bufferEnd && buffer.charAt(tokenEnd) != '\n') {
                tokenEnd++;
            }
            tokenType = PuzzTokenTypes.LINE_COMMENT;
            return;
        }

        // Whitespace (but not newlines)
        if (c == ' ' || c == '\t' || c == '\r') {
            tokenEnd = tokenStart + 1;
            while (tokenEnd < bufferEnd) {
                char next = buffer.charAt(tokenEnd);
                if (next != ' ' && next != '\t' && next != '\r') break;
                tokenEnd++;
            }
            tokenType = com.intellij.psi.TokenType.WHITE_SPACE;
            return;
        }

        // Newline
        if (c == '\n') {
            tokenEnd = tokenStart + 1;
            tokenType = PuzzTokenTypes.NEWLINE;
            return;
        }

        // String
        if (c == '"') {
            tokenEnd = tokenStart + 1;
            while (tokenEnd < bufferEnd) {
                char next = buffer.charAt(tokenEnd);
                if (next == '\\' && tokenEnd + 1 < bufferEnd) {
                    tokenEnd += 2;
                    continue;
                }
                if (next == '"') {
                    tokenEnd++;
                    break;
                }
                if (next == '\n') break;
                tokenEnd++;
            }
            tokenType = PuzzTokenTypes.STRING;
            return;
        }

        // Number
        if (Character.isDigit(c)) {
            tokenEnd = tokenStart + 1;
            while (tokenEnd < bufferEnd && (Character.isDigit(buffer.charAt(tokenEnd)) || buffer.charAt(tokenEnd) == '.')) {
                tokenEnd++;
            }
            tokenType = PuzzTokenTypes.NUMBER;
            return;
        }

        // Identifier or keyword
        if (Character.isLetter(c) || c == '_') {
            tokenEnd = tokenStart + 1;
            while (tokenEnd < bufferEnd) {
                char next = buffer.charAt(tokenEnd);
                if (!Character.isLetterOrDigit(next) && next != '_') break;
                tokenEnd++;
            }
            String word = buffer.subSequence(tokenStart, tokenEnd).toString();
            tokenType = KEYWORDS.getOrDefault(word, PuzzTokenTypes.IDENTIFIER);
            return;
        }

        // Two-character operators
        if (tokenStart + 1 < bufferEnd) {
            String two = buffer.subSequence(tokenStart, tokenStart + 2).toString();
            IElementType twoCharType = getTwoCharOperator(two);
            if (twoCharType != null) {
                tokenEnd = tokenStart + 2;
                // Check for ..= (three character)
                if (two.equals("..") && tokenEnd < bufferEnd && buffer.charAt(tokenEnd) == '=') {
                    tokenEnd++;
                    tokenType = PuzzTokenTypes.RANGE_INCL;
                } else {
                    tokenType = twoCharType;
                }
                return;
            }
        }

        // Single-character operators and delimiters
        tokenEnd = tokenStart + 1;
        tokenType = getSingleCharToken(c);
    }

    private IElementType getTwoCharOperator(String s) {
        return switch (s) {
            case "==" -> PuzzTokenTypes.EQ;
            case "!=" -> PuzzTokenTypes.NEQ;
            case "<=" -> PuzzTokenTypes.LE;
            case ">=" -> PuzzTokenTypes.GE;
            case "->" -> PuzzTokenTypes.ARROW;
            case ".." -> PuzzTokenTypes.RANGE;
            default -> null;
        };
    }

    private IElementType getSingleCharToken(char c) {
        return switch (c) {
            case '+' -> PuzzTokenTypes.PLUS;
            case '-' -> PuzzTokenTypes.MINUS;
            case '*' -> PuzzTokenTypes.STAR;
            case '/' -> PuzzTokenTypes.SLASH;
            case '%' -> PuzzTokenTypes.PERCENT;
            case '=' -> PuzzTokenTypes.EQUALS;
            case '<' -> PuzzTokenTypes.LT;
            case '>' -> PuzzTokenTypes.GT;
            case '(' -> PuzzTokenTypes.LPAREN;
            case ')' -> PuzzTokenTypes.RPAREN;
            case '[' -> PuzzTokenTypes.LBRACKET;
            case ']' -> PuzzTokenTypes.RBRACKET;
            case '{' -> PuzzTokenTypes.LBRACE;
            case '}' -> PuzzTokenTypes.RBRACE;
            case ':' -> PuzzTokenTypes.COLON;
            case ',' -> PuzzTokenTypes.COMMA;
            case '.' -> PuzzTokenTypes.DOT;
            default -> PuzzTokenTypes.BAD_CHARACTER;
        };
    }

    @NotNull
    @Override
    public CharSequence getBufferSequence() {
        return buffer;
    }

    @Override
    public int getBufferEnd() {
        return bufferEnd;
    }
}
