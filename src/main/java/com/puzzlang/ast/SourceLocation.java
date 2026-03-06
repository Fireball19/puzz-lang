package com.puzzlang.ast;

/**
 * Tracks source location (file, line, column) for AST nodes.
 * Enables precise error messages with context.
 */
public record SourceLocation(String file, int line, int column) {
    
    public static final SourceLocation UNKNOWN = new SourceLocation("<unknown>", 0, 0);
    
    /**
     * Creates a location with just line and column (file set to default).
     */
    public static SourceLocation at(int line, int column) {
        return new SourceLocation("<input>", line, column);
    }
    
    /**
     * Creates a location from ANTLR token position.
     */
    public static SourceLocation fromToken(org.antlr.v4.runtime.Token token) {
        if (token == null) return UNKNOWN;
        return new SourceLocation("<input>", token.getLine(), token.getCharPositionInLine() + 1);
    }
    
    /**
     * Creates a location from ANTLR parser context.
     */
    public static SourceLocation fromContext(org.antlr.v4.runtime.ParserRuleContext ctx) {
        if (ctx == null || ctx.getStart() == null) return UNKNOWN;
        return fromToken(ctx.getStart());
    }
    
    public SourceLocation withFile(String newFile) {
        return new SourceLocation(newFile, line, column);
    }
    
    @Override
    public String toString() {
        if (this.equals(UNKNOWN)) return "<unknown location>";
        return "%s:%d:%d".formatted(file, line, column);
    }
    
    /**
     * Formats for error message display.
     */
    public String toErrorString() {
        if (this.equals(UNKNOWN)) return "";
        return "at %s:%d:%d".formatted(file, line, column);
    }
}
