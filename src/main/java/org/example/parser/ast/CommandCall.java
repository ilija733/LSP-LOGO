package org.example.parser.ast;


import java.util.List;

public class CommandCall extends AstNode {

    private final String name;

    private final List<AstNode> arguments;

    public CommandCall(int line,int column,String name, List<AstNode> arguments){
        super(line,column);
        this. name = name;
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public List<AstNode> getArguments() {
        return arguments;
    }
}