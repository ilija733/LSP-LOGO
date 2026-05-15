LOGO LSP Server
A Language Server Protocol (LSP) implementation for the LOGO programming language,
built in Java using LSP4J.
Features:
- Syntax Highlighting — semantic tokens for keywords, procedures,
  variables, and numbers
- Go-to-Declaration — press F12 or Ctrl+Click on any procedure name
  or variable to jump to where it was declared
- Diagnostics — real-time error reporting for unknown procedures,
  undeclared variables, and wrong argument counts
  
Project Architecture:
├── lexer/
│   ├── Lexer.java            # Turns raw source text into a flat token list
│   ├── Token.java            # A single token: type, value, line, column
│   └── TokenType.java        # Enum of all token types (TO, END, NUMBER, ...)
│
├── parser/
│   ├── Parser.java           # Turns token list into an AST
│   └── ast/
│       ├── AstNode.java      # Base class — all nodes carry line/column
│       ├── ProcedureDecl.java
│       ├── CommandCall.java
│       ├── MakeStatement.java
│       ├── IfStatement.java
│       ├── RepeatStatement.java
│       ├── VariableRef.java
│       ├── BinaryExpression.java
│       ├── NumberLiteral.java
│       ├── WordLiteral.java
│       └── OutputStatement.java
│
├── analysis/
│   ├── Symbol.java               # A named declaration: name, kind, location
│   ├── SymbolTable.java          # Map of name → Symbol for one document
│   ├── SymbolTableBuilder.java   # Walks AST and populates the SymbolTable
│   ├── SemanticTokensBuilder.java # Builds encoded highlight data from tokens
│   └── DiagnosticsPublisher.java  # Checks AST against SymbolTable, pushes errors
│
├── DocumentStore.java            # In-memory map of URI → source text
├── LogoLanguageServer.java       # Main LSP entry point, declares capabilities
├── LogoTextDocumentService.java  # Handles all textDocument/* requests
├── LogoWorkspaceService.java     # Handles workspace/* events (stubs)
└── Main.java                     # Launches server over stdin/stdout
