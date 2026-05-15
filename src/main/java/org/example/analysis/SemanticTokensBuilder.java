package org.example.analysis;

import org.example.lexer.Token;
import org.example.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class SemanticTokensBuilder {

    public static final List<String> TOKEN_TYPES = List.of(
            "keyword",
            "function",
            "variable",
            "number",
            "string"
    );


    public static final List<String> TOKEN_MODIFIERS = List.of();


    private static final int TYPE_KEYWORD  = 0;
    private static final int TYPE_FUNCTION = 1;
    private static final int TYPE_VARIABLE = 2;
    private static final int TYPE_NUMBER   = 3;
    private static final int TYPE_STRING   = 4;
    private static final int SKIP          = -1;

    private final List<Token> tokens;
    private final SymbolTable symbolTable;

    public SemanticTokensBuilder(List<Token> tokens, SymbolTable symbolTable) {
        this.tokens = tokens;
        this.symbolTable = symbolTable;
    }

    public List<Integer> build() {
        List<int[]> rawTokens = new ArrayList<>();

        for (Token token : tokens) {
            int type = classifyToken(token);
            if (type == SKIP) continue;

            int line   = token.getLine() - 1;
            int col    = token.getColumn();
            int length = tokenLength(token);

            rawTokens.add(new int[]{ line, col, length, type, 0 });
        }

        return encodeToDelta(rawTokens);
    }
    private int classifyToken(Token token) {
        return switch (token.getType()) {
            case TO, END, REPEAT, IF, IFELSE, MAKE, OUTPUT, STOP -> TYPE_KEYWORD;

            case IDENTIFIER -> TYPE_FUNCTION;

            case VARIABLE -> TYPE_VARIABLE;

            case NUMBER -> TYPE_NUMBER;

            case QUOTED_WORD -> TYPE_STRING;

            default -> SKIP;
        };
    }

    private int tokenLength(Token token) {
        return switch (token.getType()) {
            case VARIABLE -> token.getValue().length() + 1;

            case QUOTED_WORD -> token.getValue().length() + 1;

            default -> token.getValue().length();
        };
    }

    private List<Integer> encodeToDelta(List<int[]> rawTokens) {
        List<Integer> data = new ArrayList<>();
        int prevLine = 0;
        int prevCol  = 0;

        for (int[] tok : rawTokens) {
            int line      = tok[0];
            int col       = tok[1];
            int length    = tok[2];
            int type      = tok[3];
            int modifiers = tok[4];

            int deltaLine = line - prevLine;
            int deltaCol  = (deltaLine == 0) ? col - prevCol : col;

            data.add(deltaLine);
            data.add(deltaCol);
            data.add(length);
            data.add(type);
            data.add(modifiers);

            prevLine = line;
            prevCol  = col;
        }

        return data;
    }
}