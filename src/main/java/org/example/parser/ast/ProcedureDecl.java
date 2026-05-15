package org.example.parser.ast;

import java.util.List;

public class ProcedureDecl extends AstNode{

    private final String name;

    private final List<String> parameters;

    private final List<AstNode> body;

    public ProcedureDecl(int line, int column,String name, List<String> parameters, List<AstNode> body){
        super(line,column);
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    public String getName() {return name;}
    public List<String> getParameters() {return parameters;}
    public List<AstNode> getBody() {return body;}
}