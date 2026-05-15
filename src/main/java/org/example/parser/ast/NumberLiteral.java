package org.example.parser.ast;

public class NumberLiteral extends AstNode {

    private final double value;

    public NumberLiteral(int line, int column, double value) {
        super(line, column);
        this.value = value;
    }

    public double getValue() { return value; }
}