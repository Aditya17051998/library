package org.paysecure.library.exception;

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
}

// throw new CustomException(HttpStatus.NOT_FOUND, "Merchant not found", "No record exists for given ID", "MerchantService.lookup()");
