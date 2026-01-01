package devnox.library.httpResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

public class ApiResponse<T> {
    private T body;
    private HttpHeaders headers;
    private HttpStatusCode status;

    public ApiResponse(T body, HttpHeaders headers, HttpStatusCode status) {
        this.body = body;
        this.headers = headers;
        this.status = status;
    }

    public T getBody() { return body; }
    public HttpHeaders getHeaders() { return headers; }
    public HttpStatusCode getStatus() { return status; }
}

