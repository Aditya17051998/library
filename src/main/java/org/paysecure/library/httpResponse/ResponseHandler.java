package org.paysecure.library.httpResponse;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


public class ResponseHandler {

    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, data));
    }

    public static ResponseEntity<ApiError> error(HttpStatus status, String message, String description, String devMsg) {
        ApiError apiError = new ApiError(status.value(), message, description, devMsg);
        return new ResponseEntity<>(apiError, status);
    }

    public static ResponseEntity<ApiError> validationError(List<ErrorFields> fieldErrors, String devMsg) {
        ApiError apiError = new ApiError(400, "Validation Failed", "Request contains invalid fields", devMsg);
        apiError.setFieldErrors(fieldErrors);
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }
}

