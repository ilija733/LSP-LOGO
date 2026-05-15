# LOGO LSP Server

A Language Server Protocol (LSP) implementation for the [LOGO programming language](https://el.media.mit.edu/logo-foundation/what_is_logo/logo_programming.html), built in Java using [LSP4J](https://github.com/eclipse-lsp4j/lsp4j).

## Features

- **Syntax Highlighting** — semantic tokens for keywords, procedures, variables, and numbers
- **Go-to-Declaration** — press `Ctrl+B` on any procedure name or variable to jump to its declaration
- **Diagnostics** — real-time error reporting for:
  - Unknown procedures
  - Undeclared variables
  - Wrong number of arguments

## Requirements

- Java 17+
- Gradle (or use the included `gradlew` wrapper)
- IntelliJ IDEA (any edition) with the [LSP4IJ](https://plugins.jetbrains.com/plugin/23257-lsp4ij) plugin

## How to Build

Clone the repository and build the fat JAR:

```bash
git clone https://github.com/ilija733/LSP-LOGO.git
cd LSP-LOGO
./gradlew jar
```

The output JAR will be at:
```
build/libs/ProjectP-1.0-SNAPSHOT.jar
```

Verify the server starts correctly:
```bash
java -jar build/libs/ProjectP-1.0-SNAPSHOT.jar
```
It should hang silently — this means it is running and waiting for an LSP client to connect. Press `Ctrl+C` to stop.

## How to Connect to IntelliJ IDEA via LSP4IJ

### Step 1 — Install LSP4IJ Plugin

In IntelliJ IDEA:
1. Go to `File` → `Settings` → `Plugins`
2. Search for **LSP4IJ**
3. Click **Install** and restart IntelliJ

### Step 2 — Register the LOGO LSP Server

1. Go to `File` → `Settings` → `Languages & Frameworks` → `Language Servers`
2. Click `+` to add a new server
3. Fill in:
   - **Name:** `LOGO LSP`
   - **Command:** `java -jar /absolute/path/to/build/libs/ProjectP-1.0-SNAPSHOT.jar`

   Example on Windows:
   ```
   java -jar C:\Users\YourName\LSP-LOGO\build\libs\ProjectP-1.0-SNAPSHOT.jar
   ```

4. Click the **Mappings** tab → **File name patterns** tab
5. Click `+` and add: `*.logo`
6. Click **Apply** → **OK**

### Step 3 — Test with a LOGO File

Create a new file called `test.logo` anywhere on your system and open it in IntelliJ.

Paste this test code which includes both valid LOGO and intentional errors:

```logo
TO SQUARE :SIZE
  REPEAT 4 [
    FORWARD :SIZE
    RIGHT 90
  ]
END

TO TRIANGLE :SIDE
  REPEAT 3 [
    FORWARD :SIDE
    RIGHT 120
  ]
END

SQUARE 100
TRIANGLE 50
UNKNOWN 999
SQUARE 10 20
```

### Expected Results

| Line | Code | Expected |
|------|------|----------|
| 1 | `TO SQUARE :SIZE` | `TO`, `END` colored as keywords; `:SIZE` colored as variable |
| 2 | `REPEAT 4 [` | `REPEAT` colored as keyword; `4` colored as number |
| 15 | `SQUARE 100` | `SQUARE` colored as function |
| 17 | `UNKNOWN 999` | Red underline — "Unknown procedure: UNKNOWN" |
| 18 | `SQUARE 10 20` | Red underline — "'SQUARE' expects 1 argument(s), got 2" |

### Testing Go-to-Declaration

1. Place your cursor on `SQUARE` on line 15
2. Press `Ctrl+B`
3. The cursor should jump to line 1 where `TO SQUARE` is declared

## Project Architecture

```
src/main/java/org/example/
├── lexer/
│   ├── Lexer.java                  # Turns raw source text into a flat token list
│   ├── Token.java                  # A single token: type, value, line, column
│   └── TokenType.java              # Enum of all token types (TO, END, NUMBER, ...)
│
├── parser/
│   ├── Parser.java                 # Turns token list into an AST
│   └── ast/
│       ├── AstNode.java            # Base class — all nodes carry line/column
│       ├── ProcedureDecl.java      # TO name :params ... END
│       ├── CommandCall.java        # Any procedure/command call with arguments
│       ├── MakeStatement.java      # MAKE "name value
│       ├── IfStatement.java        # IF / IFELSE
│       ├── RepeatStatement.java    # REPEAT n [ ... ]
│       ├── VariableRef.java        # :name
│       ├── BinaryExpression.java   # left op right
│       ├── NumberLiteral.java      # 90, 4, 100 etc.
│       ├── WordLiteral.java        # "word
│       └── OutputStatement.java    # OUTPUT expression
│
├── analysis/
│   ├── Symbol.java                 # A named declaration: name, kind, location
│   ├── SymbolTable.java            # Map of name → Symbol for one document
│   ├── SymbolTableBuilder.java     # Walks AST and populates SymbolTable
│   ├── SemanticTokensBuilder.java  # Builds encoded highlight data from tokens
│   └── DiagnosticsPublisher.java   # Checks AST against SymbolTable, pushes errors
│
├── DocumentStore.java              # In-memory map of URI → source text
├── LogoLanguageServer.java         # Main LSP entry point, declares capabilities
├── LogoTextDocumentService.java    # Handles all textDocument/* requests
├── LogoWorkspaceService.java       # Handles workspace/* events (stubs)
└── Main.java                       # Launches server over stdin/stdout
```

## How It Works — Request Flow

Every time the user types, this pipeline runs automatically:

```
User types in editor
       │
       ▼
didChange() → analyzeDocument()
                    ├── Lexer.tokenize()           turn text into tokens
                    ├── Parser.parse()             turn tokens into AST
                    ├── SymbolTableBuilder.build() collect all declarations
                    └── DiagnosticsPublisher       push errors to editor

Editor requests syntax highlighting:
       │
       ▼
semanticTokensFull()
       ├── Lexer.tokenize()
       └── SemanticTokensBuilder.build()           return encoded token array

User presses Ctrl+B (Go-to-Declaration):
       │
       ▼
declaration()
       ├── wordAt()                                extract word at cursor position
       └── symbolTable.lookup()                    return declaration location
```

## LOGO Language — Quick Reference

```logo
; Procedure declaration
TO SQUARE :SIZE
  REPEAT 4 [
    FORWARD :SIZE
    RIGHT 90
  ]
END

; Variable declaration
MAKE "COUNT 10

; Calling a procedure
SQUARE 100

; Conditional
IF :COUNT > 5 [
  PRINT "BIG
]

; If/else
IFELSE :COUNT > 5 [
  PRINT "BIG
] [
  PRINT "SMALL
]
```

Key built-in commands: `FORWARD`, `BACKWARD`, `RIGHT`, `LEFT`, `PENUP`, `PENDOWN`, `HOME`, `CLEARSCREEN`, `PRINT`, `REPEAT`
