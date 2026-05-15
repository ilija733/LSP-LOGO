package org.example.parser;

import org.example.lexer.Token;
import org.example.lexer.TokenType;
import org.example.parser.ast.*;

import java.util.ArrayList;
import java.util.List;


public class Parser{

    private final List<Token> tokens;
    private int pos;

    public Parser(List<Token> tokens){
        this.tokens = tokens;
        this.pos = 0;
    }

    public List<AstNode> parse(){
        List<AstNode> nodes = new ArrayList<>();

        while(!isAtEnd()){
            AstNode node = parseStatement();
            if(node != null) nodes.add(node);
        }

        return nodes;
    }

    private AstNode parseStatement(){
        Token current = peek();

        return switch (current.getType()){
            case TO     -> parseProcedureDecl();
            case MAKE   -> parseMakeStatement();
            case REPEAT -> parseRepeatStatement();
            case IF     -> parseIfStatement(false);
            case IFELSE -> parseIfStatement(true);
            case OUTPUT -> parseOutputStatement();
            case STOP -> {
                advance();
                yield null;
            }
            case EOF -> {
                advance();
                yield null;
            }
            default -> parseCommandCall();
        };
    }

    private ProcedureDecl parseProcedureDecl(){
        Token toToken = consume(TokenType.TO);

        Token nameToken = consume(TokenType.IDENTIFIER);
        String name = nameToken.getValue();

        List<String> params = new ArrayList<>();
        while (peek().getType() == TokenType.VARIABLE) {
            params.add(advance().getValue());
        }

        List<AstNode> body = new ArrayList<>();
        while (!isAtEnd() && peek().getType() != TokenType.END) {
            AstNode stmt = parseStatement();
            if (stmt != null) body.add(stmt);
        }

        consume(TokenType.END);

        return new ProcedureDecl(toToken.getLine(), toToken.getColumn(),name, params, body);
    }

    private MakeStatement parseMakeStatement() {
        Token makeToken = consume(TokenType.MAKE);

        Token nameToken = consume(TokenType.QUOTED_WORD);
        String varName  = nameToken.getValue();

        AstNode value = parseExpression();

        return new MakeStatement(makeToken.getLine(), makeToken.getColumn(),varName, value);
    }

    private RepeatStatement parseRepeatStatement() {
        Token repeatToken = consume(TokenType.REPEAT);
        AstNode count = parseExpression();
        List<AstNode> body = parseBlock();

        return new RepeatStatement(
                repeatToken.getLine(), repeatToken.getColumn(),count, body);
    }

    private IfStatement parseIfStatement(boolean hasElse) {
        Token ifToken = advance();
        AstNode condition = parseExpression();
        List<AstNode> thenBranch = parseBlock();
        List<AstNode> elseBranch = hasElse ? parseBlock() : null;

        return new IfStatement(ifToken.getLine(), ifToken.getColumn(),
                condition, thenBranch, elseBranch);
    }

    private OutputStatement parseOutputStatement() {
        Token outputToken = consume(TokenType.OUTPUT);
        AstNode value = parseExpression();

        return new OutputStatement(outputToken.getLine(), outputToken.getColumn(),value);
    }

    private CommandCall parseCommandCall() {
        Token nameToken = advance();
        String name = nameToken.getValue();

        List<AstNode> args = new ArrayList<>();


        while (canStartExpression(peek())) {
            args.add(parsePrimary());
        }

        return new CommandCall(nameToken.getLine(), nameToken.getColumn(),name, args);
    }

    private List<AstNode> parseBlock() {
        consume(TokenType.LBRACKET);
        List<AstNode> stmts = new ArrayList<>();

        while (!isAtEnd() && peek().getType() != TokenType.RBRACKET) {
            AstNode stmt = parseStatement();
            if (stmt != null) stmts.add(stmt);
        }

        consume(TokenType.RBRACKET);
        return stmts;
    }

    private AstNode parseExpression() {
        AstNode left = parsePrimary();

        if (isBinaryOperator(peek())) {
            Token op = advance();
            AstNode right = parsePrimary();
            return new BinaryExpression(
                    op.getLine(), op.getColumn(),
                    left, op.getValue(), right
            );
        }

        return left;
    }


    private AstNode parsePrimary() {
        Token t = peek();

        return switch (t.getType()) {
            case NUMBER -> {
                advance();
                yield new NumberLiteral(t.getLine(), t.getColumn(),Double.parseDouble(t.getValue()));
            }
            case VARIABLE -> {
                advance();
                yield new VariableRef(t.getLine(), t.getColumn(), t.getValue());
            }
            case QUOTED_WORD -> {
                advance();
                yield new WordLiteral(t.getLine(), t.getColumn(), t.getValue());
            }
            case LPAREN -> {
                advance();
                AstNode expr = parseExpression();
                consume(TokenType.RPAREN);
                yield expr;
            }
            default -> {
                advance();
                yield new NumberLiteral(t.getLine(), t.getColumn(), 0);
            }
        };
    }
//HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS
//HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS
//HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS
    private Token peek() {
        return tokens.get(pos);
    }

    // Consumes and returns current token
    private Token advance() {
        Token t = tokens.get(pos);
        if (!isAtEnd()) pos++;
        return t;
    }

    // Consumes current token, throws if it's not the expected type.
    // This is how we enforce grammar rules.
    private Token consume(TokenType expected) {
        Token t = peek();
        if (t.getType() != expected) {
            // Error recovery: return the token anyway and move on.
            // A real parser would emit a diagnostic here.
            return t;
        }
        return advance();
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    // Can this token start an expression (i.e. be an argument)?
    private boolean canStartExpression(Token t) {
        return switch (t.getType()) {
            case NUMBER, VARIABLE, QUOTED_WORD, LPAREN -> true;
            default -> false;
        };
    }

    private boolean isBinaryOperator(Token t) {
        return switch (t.getType()) {
            case PLUS, MINUS, MULTIPLY, DIVIDE,
                    EQUALS, LESS_THAN, GREATER_THAN,
                    NOT_EQUALS, LESS_EQ, GREATER_EQ -> true;
            default -> false;
        };
    }
}