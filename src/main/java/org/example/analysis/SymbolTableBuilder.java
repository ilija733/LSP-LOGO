package org.example.analysis;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.example.parser.ast.*;

import java.util.List;

public class SymbolTableBuilder {

    private final String documentUri;
    private final SymbolTable table;

    public SymbolTableBuilder(String documentUri, SymbolTable table) {
        this.documentUri = documentUri;
        this.table = table;
    }

    public void build(List<AstNode> ast) {
        table.clear();
        for (AstNode node : ast) {
            visit(node);
        }
    }

    private void visit(AstNode node) {
        if (node instanceof ProcedureDecl p) {
            visitProcedureDecl(p);

        } else if (node instanceof MakeStatement m) {
            visitMakeStatement(m);

        } else if (node instanceof RepeatStatement r) {
            for (AstNode child : r.getBody()) visit(child);

        } else if (node instanceof IfStatement i) {
            for (AstNode child : i.getThenBranch()) visit(child);
            if (i.hasElseBranch()) {
                for (AstNode child : i.getElseBranch()) visit(child);
            }
        }
    }

    private void visitProcedureDecl(ProcedureDecl p) {
        table.define(new Symbol(
                p.getName(),
                Symbol.Kind.PROCEDURE,
                makeLocation(p.getLine(), p.getColumn()),
                p.getParameters().size()
        ));

        for (String param : p.getParameters()) {
            table.define(new Symbol(
                    param,
                    Symbol.Kind.VARIABLE,
                    makeLocation(p.getLine(), p.getColumn())
            ));
        }

        for (AstNode child : p.getBody()) visit(child);
    }

    private void visitMakeStatement(MakeStatement m) {
        table.define(new Symbol(
                m.getVariableName(),
                Symbol.Kind.VARIABLE,
                makeLocation(m.getLine(), m.getColumn())
        ));
    }

    private Location makeLocation(int line, int column) {
        Position pos = new Position(line - 1, column);
        return new Location(documentUri, new Range(pos, pos));
    }
}