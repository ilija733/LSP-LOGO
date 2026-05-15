package org.example.parser.ast;

import java.util.List;

public class RepeatStatement extends AstNode{

    private final AstNode count;

    private final List<AstNode> body;

    public RepeatStatement(int line, int column, AstNode count,List<AstNode>  body){
        super(line,column);
        this.count = count;
        this.body = body;
    }

    public AstNode getCount() { return count; }
    public List<AstNode> getBody() { return body; }
}