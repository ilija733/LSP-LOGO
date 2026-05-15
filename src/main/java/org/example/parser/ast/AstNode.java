package org.example.parser.ast;

public abstract class AstNode{
    private final int line;
    private final int column;

    protected AstNode(int line,int column){
        this.line = line;
        this.column = column;
    }
    public int getLine(){return line;}
    public int getColumn(){return column;}

}