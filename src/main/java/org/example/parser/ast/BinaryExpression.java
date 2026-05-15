package org.example.parser.ast;

public class BinaryExpression extends AstNode{

    private final AstNode left;
    private final String operator;
    private final AstNode right;

    public BinaryExpression(int line, int column, AstNode left, String operator, AstNode right){
        super(line, column);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public AstNode getLeft() {
        return left;
    }

    public String getOperator() {
        return operator;
    }

    public AstNode getRight() {
        return right;
    }
}