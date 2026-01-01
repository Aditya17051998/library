package devnox.library.httpResponse;

import lombok.Data;
import java.util.List;

@Data
public class ApiError {
    private int status;
    private String message;
    private String description;
    private String developerMessage;
    private List<ErrorFields> fieldErrors;

    public ApiError(int status, String message, String description, String developerMessage) {
        this.status = status;
        this.message = message;
        this.description = description;
        this.developerMessage = developerMessage;
    }

    public void setFieldErrors(List<ErrorFields> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

}

