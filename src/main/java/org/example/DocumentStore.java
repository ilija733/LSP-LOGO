package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DocumentStore {

    private final Map<String, String> documents = new HashMap<>();

    public void put(String uri, String text) {
        documents.put(uri, text);
    }

    public Optional<String> get(String uri) {
        return Optional.ofNullable(documents.get(uri));
    }

    public void remove(String uri) {
        documents.remove(uri);
    }
}