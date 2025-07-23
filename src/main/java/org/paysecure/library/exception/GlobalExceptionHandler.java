package org.paysecure.library.exception;

import org.paysecure.library.httpResponse.ApiError;
import org.paysecure.library.httpResponse.ErrorFields;
import org.paysecure.library.httpResponse.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 🔴 Validation Exception: Triggered when @Valid fails in @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<ErrorFields> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fieldError -> new ErrorFields(fieldError.getField(), fieldError.getDefaultMessage()))
            .collect(Collectors.toList());

        return ResponseHandler.validationError(fieldErrors, ex.getMessage());
    }

    // 🔴 Path Variable Missing
    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ApiError> handleMissingPathVariable(MissingPathVariableException ex) {
        return ResponseHandler.error(
                HttpStatus.BAD_REQUEST,
                "Path variable is missing",
                ex.getMessage(),
                ex.getVariableName()
        );
    }

    // 🔴 Request Parameter Missing (e.g., ?id= missing)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingServletRequestParam(MissingServletRequestParameterException ex) {
        return ResponseHandler.error(
                HttpStatus.BAD_REQUEST,
                "Request parameter is missing",
                ex.getMessage(),
                ex.getParameterName()
        );
    }

    // 🔴 Type Mismatch (e.g., expecting int but got string)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseHandler.error(
                HttpStatus.BAD_REQUEST,
                "Parameter type mismatch",
                "Expected type: " + ex.getRequiredType(),
                ex.getName() + "=" + ex.getValue()
        );
    }

    // 🔴 Binding errors from query/form params (for @ModelAttribute)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> handleBindException(BindException ex) {
        List<ErrorFields> fieldErrors = ex.getFieldErrors()
            .stream()
            .map(err -> new ErrorFields(err.getField(), err.getDefaultMessage()))
            .collect(Collectors.toList());

        return ResponseHandler.validationError(fieldErrors, ex.getMessage());
    }

    // 🔴 Request Body Parsing error (e.g., malformed JSON)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleJsonParseError(HttpMessageNotReadableException ex) {
        return ResponseHandler.error(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON request",
                ex.getMessage(),
                "Check request body structure"
        );
    }

    // 🔴 Unsupported Media Type (e.g., XML instead of JSON)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        return ResponseHandler.error(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Media type not supported",
                ex.getMessage(),
                "Supported: " + ex.getSupportedMediaTypes()
        );
    }

    // 🔴 Unsupported Method (e.g., POST when only GET is allowed)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return ResponseHandler.error(
                HttpStatus.METHOD_NOT_ALLOWED,
                "HTTP method not supported",
                ex.getMessage(),
                "Allowed: " + ex.getSupportedHttpMethods()
        );
    }

    // 🔴 Required Headers not present or invalid
    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<ApiError> handleServletRequestBindingException(ServletRequestBindingException ex) {
        return ResponseHandler.error(
                HttpStatus.BAD_REQUEST,
                "Missing or invalid headers",
                ex.getMessage(),
                "Header issue"
        );
    }

    // // 🔴 Access Denied (Spring Security)
    // @ExceptionHandler(AccessDeniedException.class)
    // public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
    //     return ResponseHandler.error(
    //             HttpStatus.FORBIDDEN,
    //             "Access denied",
    //             ex.getMessage(),
    //             "You do not have permission to access this resource"
    //     );
    // }

    // 🔴 Your own business exceptions
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiError> handleCustomException(CustomException ex) {
        return ResponseHandler.error(
                ex.getStatus(),
                ex.getMessage(),
                ex.getDescription(),
                ex.getDevMessage()
        );
    }

    // ⚠️ Fallback for everything else
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        return ResponseHandler.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong",
                "Unexpected error",
                ex.getMessage()
        );
    }
}
