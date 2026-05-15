package org.example;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;
import org.example.analysis.SemanticTokensBuilder;
import org.eclipse.lsp4j.SemanticTokensLegend;
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions;

import java.util.concurrent.CompletableFuture;

public class LogoLanguageServer implements LanguageServer, LanguageClientAware {

    private LanguageClient client;
    private final DocumentStore documentStore = new DocumentStore();
    private final LogoTextDocumentService textDocumentService =
            new LogoTextDocumentService(documentStore);
    private final LogoWorkspaceService workspaceService =
            new LogoWorkspaceService();

    @Override
    public void initialized(InitializedParams params) {
        // Nothing yet
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params){
        ServerCapabilities capabilities = new ServerCapabilities();

        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);

        SemanticTokensLegend legend = new SemanticTokensLegend(
                SemanticTokensBuilder.TOKEN_TYPES,
                SemanticTokensBuilder.TOKEN_MODIFIERS
        );
        SemanticTokensWithRegistrationOptions semanticOptions =
                new SemanticTokensWithRegistrationOptions();
        semanticOptions.setLegend(legend);
        semanticOptions.setFull(true);
        capabilities.setSemanticTokensProvider(semanticOptions);

        capabilities.setDeclarationProvider(true);

        return CompletableFuture.completedFuture(new InitializeResult(capabilities));
    }

    @Override
    public void exit() {
        System.exit(0);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return workspaceService;
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
        this.textDocumentService.setClient(client);
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return CompletableFuture.completedFuture(null);
    }
}