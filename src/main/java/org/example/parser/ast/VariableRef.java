package org.example.parser.ast;

public class VariableRef extends AstNode{

    private final String name;

    public VariableRef(int line, int column, String name){
        super(line,column);
        this.name=name;
    }

    public String getName() {
        return name;
    }
}
