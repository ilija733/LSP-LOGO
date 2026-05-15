package org.example.parser.ast;

public class MakeStatement extends AstNode {

    private final String variableName;

    private final AstNode value;

    public MakeStatement(int line, int column, String variableName, AstNode value) {
        super(line, column);
        this.variableName = variableName;
        this.value= value;
    }

    public String getVariableName() { return variableName; }
    public AstNode getValue() { return value; }
}