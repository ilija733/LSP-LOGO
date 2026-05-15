package org.example.analysis;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.example.parser.ast.*;

import java.util.*;

public class DiagnosticsPublisher {

    private static final Set<String> BUILT_INS = Set.of(
            "FORWARD", "FD", "BACKWARD", "BK",
            "RIGHT", "RT", "LEFT", "LT",
            "PENUP", "PU", "PENDOWN", "PD",
            "SETXY", "SETX", "SETY",
            "HOME", "CLEARSCREEN", "CS",
            "PRINT", "SHOW", "TYPE",
            "MAKE", "LOCAL",
            "WAIT", "STOP", "OUTPUT",
            "SETPENCOLOR", "SETPC", "SETPENSIZE",
            "CIRCLE", "ARC"
    );

    private final LanguageClient client;
    private final String documentUri;
    private final SymbolTable symbolTable;

    public DiagnosticsPublisher(LanguageClient client,
                                String documentUri,
                                SymbolTable symbolTable) {
        this.client = client;
        this.documentUri = documentUri;
        this.symbolTable = symbolTable;
    }

    public void publish(List<AstNode> ast) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        for (AstNode node : ast) {
            collectDiagnostics(node, diagnostics);
        }

        PublishDiagnosticsParams publishParams =
                new PublishDiagnosticsParams(documentUri, diagnostics);
        client.publishDiagnostics(publishParams);
    }

    private void collectDiagnostics(AstNode node, List<Diagnostic> out) {

        if (node instanceof CommandCall call) {
            checkCommandCall(call, out);
            for (AstNode arg : call.getArguments()) collectDiagnostics(arg, out);

        } else if (node instanceof VariableRef ref) {
            checkVariableRef(ref, out);

        } else if (node instanceof ProcedureDecl decl) {
            for (AstNode child : decl.getBody()) collectDiagnostics(child, out);

        } else if (node instanceof RepeatStatement repeat) {
            collectDiagnostics(repeat.getCount(), out);
            for (AstNode child : repeat.getBody()) collectDiagnostics(child, out);

        } else if (node instanceof IfStatement ifStmt) {
            collectDiagnostics(ifStmt.getCondition(), out);
            for (AstNode child : ifStmt.getThenBranch()) collectDiagnostics(child, out);
            if (ifStmt.hasElseBranch()) {
                for (AstNode child : ifStmt.getElseBranch()) collectDiagnostics(child, out);
            }

        } else if (node instanceof MakeStatement make) {
            collectDiagnostics(make.getValue(), out);

        } else if (node instanceof BinaryExpression bin) {
            collectDiagnostics(bin.getLeft(), out);
            collectDiagnostics(bin.getRight(), out);

        } else if (node instanceof OutputStatement output) {
            collectDiagnostics(output.getValue(), out);
        }
    }

    private void checkCommandCall(CommandCall call, List<Diagnostic> out) {
        String name = call.getName().toUpperCase();

        if (BUILT_INS.contains(name)) return;

        Optional<Symbol> symbol = symbolTable.lookup(name);

        if (symbol.isEmpty()) {
            out.add(makeDiagnostic(
                    call.getLine(), call.getColumn(),
                    name.length(),
                    "Unknown procedure: " + name,
                    DiagnosticSeverity.Error
            ));
            return;
        }

        if (symbol.get().getKind() == Symbol.Kind.PROCEDURE) {
            int expected = symbol.get().getParameterCount();
            int actual   = call.getArguments().size();
            if (expected != actual) {
                out.add(makeDiagnostic(
                        call.getLine(), call.getColumn(),
                        name.length(),
                        String.format("'%s' expects %d argument(s), got %d", name, expected, actual),
                        DiagnosticSeverity.Error
                ));
            }
        }
    }

    private void checkVariableRef(VariableRef ref, List<Diagnostic> out) {
        String name = ref.getName().toUpperCase();

        if (symbolTable.lookup(name).isEmpty()) {
            out.add(makeDiagnostic(
                    ref.getLine(), ref.getColumn(),
                    name.length() + 1,
                    "Undefined variable: " + name,
                    DiagnosticSeverity.Error
            ));
        }
    }

    private Diagnostic makeDiagnostic(int line, int col, int length, String message, DiagnosticSeverity severity) {

        Position start = new Position(line - 1, col);
        Position end   = new Position(line - 1, col + length);
        Range range    = new Range(start, end);

        Diagnostic diagnostic = new Diagnostic();
        diagnostic.setRange(range);
        diagnostic.setMessage(message);
        diagnostic.setSeverity(severity);
        diagnostic.setSource("logo-lsp");
        return diagnostic;
    }
}