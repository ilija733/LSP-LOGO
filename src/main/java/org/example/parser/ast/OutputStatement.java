package org.example.parser.ast;

public class OutputStatement extends AstNode {

    private final AstNode value;

    public OutputStatement(int line, int column, AstNode value) {
        super(line, column);
        this.value = value;
    }

    public AstNode getValue() { return value; }
}