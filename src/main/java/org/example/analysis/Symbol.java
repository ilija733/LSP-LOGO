package org.example.analysis;

import org.eclipse.lsp4j.Location;

public class Symbol {

    public enum Kind { PROCEDURE, VARIABLE }

    private final String name;
    private final Kind kind;
    private final Location location;
    private final int parameterCount;

    public Symbol(String name, Kind kind, Location location, int parameterCount) {
        this.name = name;
        this.kind = kind;
        this.location = location;
        this.parameterCount = parameterCount;
    }

    public Symbol(String name, Kind kind, Location location) {
        this(name, kind, location, 0);
    }

    public String getName() { return name; }
    public Kind getKind() { return kind; }
    public Location getLocation() { return location; }
    public int getParameterCount() { return parameterCount; }
}