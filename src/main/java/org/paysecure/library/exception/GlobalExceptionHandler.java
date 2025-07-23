package org.paysecure.library.exception;

import org.paysecure.library.httpResponse.ApiError;
import org.paysecure.library.httpResponse.ErrorFields;
import org.paysecure.library.httpResponse.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.stream.Collectors;
import java.util.List;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;


// Without global handler
// @PostMapping("/merchant")
// public ResponseEntity<?> createMerchant(@RequestBody @Valid MerchantRequest request) {
//     try {
//         // validation is handled by @Valid
//         merchantService.save(request);
//         return ResponseEntity.ok("Merchant saved");
//     } catch (MethodArgumentNotValidException ex) {
//         List<String> errors = ex.getBindingResult().getFieldErrors()
//                 .stream().map(e -> e.getDefaultMessage())
//                 .collect(Collectors.toList());

//         return ResponseEntity.badRequest().body(errors);
//     } catch (Exception ex) {
//         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
//     }
// }


// With global handler
// @PostMapping("/merchant")
// public ResponseEntity<?> createMerchant(@RequestBody @Valid MerchantRequest request) {
//     merchantService.save(request);
//     return ResponseEntity.ok("Merchant saved");
// }



@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<ErrorFields> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fieldError -> new ErrorFields(fieldError.getField(), fieldError.getDefaultMessage()))
            .collect(Collectors.toList());

        return ResponseHandler.validationError(fieldErrors, ex.getMessage());
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiError> handleCustomException(CustomException ex) {
        return ResponseHandler.error(ex.getStatus(), ex.getMessage(), ex.getDescription(), ex.getDevMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        return ResponseHandler.error(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", "Unexpected error", ex.getMessage());
    }

    // @ExceptionHandler(HttpMessageNotReadableException.class)
    // public ResponseEntity<ApiError> handleJsonParseError(...) { ... }

    // @ExceptionHandler(AccessDeniedException.class)
    // public ResponseEntity<ApiError> handleForbidden(...) { ... }

}

