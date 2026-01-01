package devnox.library.httpResponse;

import lombok.Data;

@Data
public class ErrorFields {
    private String field;
    private String error;

    public ErrorFields(String field, String error) {
        this.field = field;
        this.error = error;
    }

}

