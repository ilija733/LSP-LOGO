package org.example.parser.ast;

public class WordLiteral extends AstNode {

    private final String value;

    public WordLiteral(int line, int column, String value) {
        super(line, column);
        this.value = value;
    }

    public String getValue() { return value; }
}