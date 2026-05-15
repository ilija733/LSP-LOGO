package org.example.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Lexer{
    private final String source;
    private int pos;
    private int line;
    private int column;

    private static final Set<String> KEYWORDS = Set.of(
            "TO","END","MAKE","REPEAT","IF","IFELSE","WHILE","OUTPUT","STOP","PRINT"
    );

    public Lexer(String source){
        this.source = source;
        this.pos = 0;
        this.line = 1;
        this.column = 0;
    }

    public List<Token> tokenize(){
        List<Token> tokens = new ArrayList<>();

        while(!isAtEnd()){
            skipWhiteSpaceAndComments();
            if(isAtEnd()) break;

            Token token = nextToken();
            if(token != null){
                tokens.add(token);
            }
        }

        tokens.add(new Token(TokenType.EOF, "",line,column));
        return tokens;
    }

    private Token nextToken(){
        int tokenLine = line;
        int tokenColumn = column;
        char c = advance();

        if(Character.isDigit(c)){
            return readNumber(c,tokenLine,tokenColumn);
        }

        if(c == ':'){
            return readVariable(tokenLine,tokenColumn);
        }

        if(c == '"'){
            return readQuotedWord(tokenLine,tokenColumn);
        }

        if(Character.isLetter(c)){
            return readIdentifierOrKeyword(c,tokenLine,tokenColumn);
        }

        return switch (c){
            case '[' -> new Token(TokenType.LBRACKET, "[",tokenLine,tokenColumn);
            case ']' -> new Token(TokenType.RBRACKET, "]",tokenLine,tokenColumn);
            case '(' -> new Token(TokenType.LPAREN, "(",tokenLine,tokenColumn);
            case ')' -> new Token(TokenType.RPAREN, ")",tokenLine,tokenColumn);
            case '+' -> new Token(TokenType.PLUS, "+",tokenLine,tokenColumn);
            case '-' -> new Token(TokenType.MINUS, "-",tokenLine,tokenColumn);
            case '*' -> new Token(TokenType.MULTIPLY, "*",tokenLine,tokenColumn);
            case '/' -> new Token(TokenType.DIVIDE, "/",tokenLine,tokenColumn);
            case '=' -> new Token(TokenType.EQUALS, "=",tokenLine,tokenColumn);
            case '<' -> {
                if(match('>')) yield new Token(TokenType.NOT_EQUALS,"<>",tokenLine,tokenColumn);
                if (match('=')) yield new Token(TokenType.LESS_EQ, "<=",tokenLine,tokenColumn);
                yield new Token(TokenType.LESS_THAN, "<",tokenLine,tokenColumn);
            }
            case '>'-> {
                if(match('=')) yield new Token(TokenType.GREATER_EQ,">=",tokenLine,tokenColumn);
                yield new Token(TokenType.GREATER_THAN, ">",tokenLine,tokenColumn);
            }
            default -> {
                yield null;
            }
        };
    }

    private Token readNumber(char first, int tokenLine, int tokenColumn){
        StringBuilder sb = new StringBuilder();
        sb.append(first );

        while(!isAtEnd() && Character.isDigit(peek())){
            sb.append(advance());
        }

        if(!isAtEnd() && peek() == '.' && isDigit(peekNext())){
            sb.append(advance());
            while(!isAtEnd() && Character.isDigit(peek())){
                sb.append(advance());
            }
        }

        return new Token(TokenType.NUMBER, sb.toString(),tokenLine,tokenColumn);

    }

    private Token readVariable(int tokenLine, int tokenColumn){
        StringBuilder sb = new StringBuilder();

        while(!isAtEnd() && isIdentifierChar(peek())){
            sb.append(advance());
        }

        return new Token(TokenType.VARIABLE, sb.toString().toUpperCase(), tokenLine,tokenColumn);
    }

    private Token readQuotedWord(int tokenLine, int tokenColumn){
        StringBuilder sb = new StringBuilder();

        while(!isAtEnd() && !Character.isWhitespace(peek())
                && peek() != '[' && peek() != ']'
                && peek() != '(' && peek() != ')'){
            sb.append(advance());
        }

        return new Token(TokenType.QUOTED_WORD,sb.toString().toUpperCase(),tokenLine,tokenColumn);
    }

    private Token readIdentifierOrKeyword(char first, int tokenLine,int tokenColumn){
        StringBuilder sb = new StringBuilder();
        sb.append(first);

        while(!isAtEnd() && isIdentifierChar(peek())){
            sb.append(advance());
        }

        String word = sb.toString().toUpperCase();

        if(KEYWORDS.contains(word)){
            TokenType type = TokenType.valueOf(word);
            return new Token(type,word,tokenLine,tokenColumn);

        }

        return new Token(TokenType.IDENTIFIER, word, tokenLine,tokenColumn);
    }

    private void skipWhiteSpaceAndComments(){
        while(!isAtEnd()){
            char c = peek();

            if(c == ' ' || c == '\r' || c == '\t'){
                advance();
            } else if (c == '\n') {
                advance();
                line++;
                column=0;
            } else if (c == ';') {
                while (!isAtEnd() && peek() != '\n'){
                    advance();
                }
            }else{
                break;
            }
        }
    }

    private char peek(){
        if(isAtEnd()) return '\0';
        return source.charAt(pos);
    }

    private char peekNext(){
        if(pos + 1 >= source.length()) return '\0';
        return source.charAt(pos +1);
    }

    private char advance(){
        char c = source.charAt(pos++);
        column++;
        return c;
    }

    private boolean match(char expected){
        if(isAtEnd()) return false;

        if(source.charAt(pos) != expected) return false;

        advance();
        return true;
    }

    private boolean isAtEnd(){
        return pos>= source.length();
    }

    private boolean isDigit(char c){
        return c >= '0' && c <= '9';
    }

    private boolean isIdentifierChar(char c){
        return Character.isLetterOrDigit(c) || c =='_';
    }
}