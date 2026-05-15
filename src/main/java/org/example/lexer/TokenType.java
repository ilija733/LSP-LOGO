package org.example.lexer;

public enum TokenType {
    //Keywords Keywords Keywords
    TO,
    END,
    MAKE,
    REPEAT,
    IF,
    IFELSE,
    WHILE,
    OUTPUT,
    STOP,
    PRINT,
    //Identifiers Identifiers Identifiers
    IDENTIFIER,
    //QUOTED QUOTED QUOTED
    QUOTED_WORD,
    //Literals Literals Literals
    NUMBER,
    //Brackets Brackets Brackets
    LBRACKET,
    RBRACKET,
    //Parentheses Parentheses Parentheses
    LPAREN,
    RPAREN,
    //Aritmetic Aritmetic Aritmetic
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    //COMPARASION COMPARASION COMPARASION
    EQUALS,
    LESS_THAN,
    GREATER_THAN,
    NOT_EQUALS,
    LESS_EQ,
    GREATER_EQ,
    //VARIABLE VARIABLE VARIABLE
    VARIABLE,
    //END END END
    EOF
}