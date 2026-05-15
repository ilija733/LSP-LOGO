package org.example;

import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws Exception {

        InputStream in = System.in;
        OutputStream out = System.out;

        LogoLanguageServer server = new LogoLanguageServer();

        var launcher = LSPLauncher.createServerLauncher(server, in, out);

        LanguageClient client = launcher.getRemoteProxy();

        server.connect(client);

        Future<?> listening = launcher.startListening();
        listening.get();
    }
}
/*package org.example;

import org.example.lexer.Lexer;
import org.example.lexer.Token;
import org.example.parser.Parser;
import org.example.parser.ast.*;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        String testCode = """
                TO SQUARE :SIZE
                  REPEAT 4 [
                    FORWARD :SIZE
                    RIGHT 90
                  ]
                END
                """;

        Lexer lexer = new Lexer(testCode);
        List<Token> tokens = lexer.tokenize();

        Parser parser = new Parser(tokens);
        List<AstNode> tree = parser.parse();

        for (AstNode node : tree) {
            printNode(node, 0);
        }
    }

    static void printNode(AstNode node, int indent) {
        String pad = "  ".repeat(indent);
        if (node instanceof ProcedureDecl p) {
            System.err.println(pad + "ProcedureDecl: " + p.getName()
                    + " params=" + p.getParameters());
            for (AstNode child : p.getBody()) printNode(child, indent + 1);

        } else if (node instanceof RepeatStatement r) {
            System.err.println(pad + "Repeat:");
            printNode(r.getCount(), indent + 1);
            for (AstNode child : r.getBody()) printNode(child, indent + 1);

        } else if (node instanceof CommandCall c) {
            System.err.println(pad + "CommandCall: " + c.getName()
                    + " args=" + c.getArguments().size());

        } else if (node instanceof NumberLiteral n) {
            System.err.println(pad + "Number: " + n.getValue());

        } else if (node instanceof VariableRef v) {
            System.err.println(pad + "VariableRef: " + v.getName());
        }
    }
}*/