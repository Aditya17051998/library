package devnox.library;

public class LibraryService {

    public String getGreeting(String lang) {
        return switch (lang.toLowerCase()) {
            case "en" -> "Hello aditya";
            case "fr" -> "Bonjour aditya";
            case "es" -> "Hola aditya";
            case "de" -> "Hallo aditya";
            default -> "Hello (default)";
        };
    }
}
