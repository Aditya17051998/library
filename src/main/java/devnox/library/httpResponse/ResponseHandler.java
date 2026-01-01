package devnox.library.httpResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;

public class ResponseHandler {

    // 200 OK
    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(new ApiResponse<>(data, null, HttpStatus.OK));
    }

    // 201 Created
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(data, null, HttpStatus.CREATED));
    }

    // 204 No Content
    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    // 400 Bad Request
    public static ResponseEntity<ApiError> badRequest(String message, String description, String devMsg) {
        return error(HttpStatus.BAD_REQUEST, message, description, devMsg);
    }

    // 401 Unauthorized
    public static ResponseEntity<ApiError> unauthorized(String message, String description, String devMsg) {
        return error(HttpStatus.UNAUTHORIZED, message, description, devMsg);
    }

    // 403 Forbidden
    public static ResponseEntity<ApiError> forbidden(String message, String description, String devMsg) {
        return error(HttpStatus.FORBIDDEN, message, description, devMsg);
    }

    // 404 Not Found
    public static ResponseEntity<ApiError> notFound(String message, String description, String devMsg) {
        return error(HttpStatus.NOT_FOUND, message, description, devMsg);
    }

    // 409 Conflict
    public static ResponseEntity<ApiError> conflict(String message, String description, String devMsg) {
        return error(HttpStatus.CONFLICT, message, description, devMsg);
    }

    // 422 Unprocessable Entity
    public static ResponseEntity<ApiError> unprocessable(String message, String description, String devMsg) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, message, description, devMsg);
    }

    // 500 Internal Server Error
    public static ResponseEntity<ApiError> internalServerError(String message, String description, String devMsg) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, message, description, devMsg);
    }

    // Generic error builder
    public static ResponseEntity<ApiError> error(HttpStatus status, String message, String description, String devMsg) {
        ApiError apiError = new ApiError(status.value(), message, description, devMsg);
        return new ResponseEntity<>(apiError, status);
    }

    // Validation error (400)
    public static ResponseEntity<ApiError> validationError(List<ErrorFields> fieldErrors, String devMsg) {
        ApiError apiError = new ApiError(400, "Validation Failed", "Request contains invalid fields", devMsg);
        apiError.setFieldErrors(fieldErrors);
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }
}
