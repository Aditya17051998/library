package org.paysecure.library;

public class LibraryService {

    public String getGreeting(String lang) {
        return switch (lang.toLowerCase()) {
            case "en" -> "Hello";
            case "fr" -> "Bonjour";
            case "es" -> "Hola";
            case "de" -> "Hallo";
            default -> "Hello (default)";
        };
    }
}
