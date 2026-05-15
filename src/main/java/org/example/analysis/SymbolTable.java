package org.example.analysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SymbolTable {

    private final Map<String,Symbol> symbols = new HashMap<>();

    public void define(Symbol symbol){
        symbols.put(symbol.getName().toUpperCase(),symbol);
    }

    public Optional<Symbol> lookup(String name){
        return Optional.ofNullable(symbols.get(name.toUpperCase()));
    }

    public Collection<Symbol> allSymbols(){
        return symbols.values();
    }

    public void clear(){
        symbols.clear();
    }
}