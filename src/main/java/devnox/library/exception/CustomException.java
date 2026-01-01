package devnox.library.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException {
    private final HttpStatus status;
    private final String description;
    private final String devMessage;

    public CustomException(HttpStatus status, String message, String description, String devMessage) {
        super(message);
        this.status = status;
        this.description = description;
        this.devMessage = devMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getDevMessage() {
        return devMessage;
    }

    // 🔹 Static methods with "throw" prefix

    public static CustomException throwBadRequestException(String message, String description, String devMessage) {
        return new CustomException(HttpStatus.BAD_REQUEST, message, description, devMessage);
    }

    public static CustomException throwUnauthorizedException(String message, String description, String devMessage) {
        return new CustomException(HttpStatus.UNAUTHORIZED, message, description, devMessage);
    }

    public static CustomException throwForbiddenException(String message, String description, String devMessage) {
        return new CustomException(HttpStatus.FORBIDDEN, message, description, devMessage);
    }

    public static CustomException throwNotFoundException(String message, String description, String devMessage) {
        return new CustomException(HttpStatus.NOT_FOUND, message, description, devMessage);
    }

    public static CustomException throwConflictException(String message, String description, String devMessage) {
        return new CustomException(HttpStatus.CONFLICT, message, description, devMessage);
    }

    public static CustomException throwInternalServerErrorException(String message, String description, String devMessage) {
        return new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, message, description, devMessage);
    }

    public static CustomException throwCustomException(HttpStatus status, String message, String description, String devMessage) {
        return new CustomException(status, message, description, devMessage);
    }
}
