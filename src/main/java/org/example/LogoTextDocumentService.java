package org.example;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.example.analysis.SymbolTable;
import org.example.analysis.SymbolTableBuilder;
import org.example.lexer.Lexer;
import org.example.parser.Parser;
import org.example.parser.ast.AstNode;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.example.analysis.SemanticTokensBuilder;
import org.example.lexer.Token;
import org.eclipse.lsp4j.DeclarationParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.example.analysis.Symbol;
import org.example.analysis.DiagnosticsPublisher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LogoTextDocumentService implements TextDocumentService {

    private final DocumentStore documentStore;
    private LanguageClient client;

    private final Map<String, SymbolTable> symbolTables = new HashMap<>();

    public LogoTextDocumentService(DocumentStore documentStore) {
        this.documentStore = documentStore;
    }

    public void setClient(LanguageClient client) {
        this.client = client;
    }

    public SymbolTable getSymbolTable(String uri){
        return symbolTables.getOrDefault(uri,new SymbolTable());
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        String text = params.getTextDocument().getText();
        documentStore.put(uri, text);

        analyzeDocument(uri,text);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        String newText = params.getContentChanges().get(0).getText();
        documentStore.put(uri, newText);

        analyzeDocument(uri, newText);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        documentStore.remove(uri);

        symbolTables.remove(uri);
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {

    }

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
        String uri = params.getTextDocument().getUri();

        String text = documentStore.get(uri).orElse("");

        try {

            Lexer lexer = new Lexer(text);
            List<Token> tokens = lexer.tokenize();

            SymbolTable table = getSymbolTable(uri);


            SemanticTokensBuilder builder = new SemanticTokensBuilder(tokens, table);
            List<Integer> data = builder.build();

            return CompletableFuture.completedFuture(new SemanticTokens(data));

        } catch (Exception e) {

            System.err.println("semanticTokensFull error: " + e.getMessage());
            return CompletableFuture.completedFuture(new SemanticTokens(List.of()));
        }
    }

    private void analyzeDocument(String uri, String text){

        try {
            Lexer lexer = new Lexer(text);
            List<org.example.lexer.Token> tokens = lexer.tokenize();

            Parser parser = new Parser(tokens);
            List<AstNode> ast = parser.parse();

            SymbolTable table = symbolTables.computeIfAbsent(uri, k -> new SymbolTable());
            SymbolTableBuilder builder = new SymbolTableBuilder(uri, table);
            builder.build(ast);

            //(Phase 9)(Phase 9)(Phase 9)(Phase 9)
            if (client != null) {
                new DiagnosticsPublisher(client, uri, table).publish(ast);
            }
        }catch (Exception e){
            System.err.println("Analysis error for " + uri + ": " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>>
    declaration(DeclarationParams params) {

        String uri = params.getTextDocument().getUri();

        int line = params.getPosition().getLine() + 1;
        int col  = params.getPosition().getCharacter() + 1;

        String text = documentStore.get(uri).orElse("");

        String word = wordAt(text, line, col);

        if (word.isEmpty()) {
            return CompletableFuture.completedFuture(Either.forLeft(List.of()));
        }

        SymbolTable table = getSymbolTable(uri);
        return table.lookup(word)
                .map(symbol -> {
                    Location location = symbol.getLocation();
                    return CompletableFuture.completedFuture(
                            Either.<List<? extends Location>,
                                    List<? extends LocationLink>>
                                    forLeft(List.of(location))
                    );
                })
                .orElse(CompletableFuture.completedFuture(Either.forLeft(List.of())));
    }

    private String wordAt(String text, int line, int col) {

        String[] lines = text.split("\r?\n", -1);

        if (line < 1 || line > lines.length) return "";

        String sourceLine = lines[line - 1];

        int idx = col - 1;
        if (idx < 0 || idx >= sourceLine.length()) return "";

        if (!isWordChar(sourceLine.charAt(idx))) return "";

        int start = idx;
        while (start > 0 && isWordChar(sourceLine.charAt(start - 1))) {
            start--;
        }

        int end = idx;
        while (end < sourceLine.length() - 1 && isWordChar(sourceLine.charAt(end + 1))) {
            end++;
        }

        return sourceLine.substring(start, end + 1).toUpperCase();
    }

    private boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '-';
    }

}
