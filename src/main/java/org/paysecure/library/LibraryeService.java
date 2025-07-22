package org.paysecure.library;

public class LibraryeService {

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
