package org.example.parser.ast;

import java.util.List;

public class IfStatement extends AstNode{
    private final AstNode condition;
    private final List<AstNode> thenBranch;

    private final List<AstNode> elseBranch;

    public IfStatement(int line,int column, AstNode condition,List<AstNode> thenBranch,List<AstNode> elseBranch){
        super(line,column);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;

    }

    public AstNode getCondition() { return condition; }
    public List<AstNode> getThenBranch() { return thenBranch; }
    public List<AstNode> getElseBranch() { return elseBranch; }
    public boolean hasElseBranch() { return elseBranch != null; }
}