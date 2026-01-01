package devnox.library.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import devnox.library.logging.LoggingContext;
import devnox.library.logging.RequestLoggingContext;
import devnox.library.logging.WhiteListedKeys;
import devnox.library.httpResponse.ApiResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Flexible HTTP Client for outgoing API calls
 * Supports builder pattern, large responses, and structured logging
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HttpApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    private LoggingContext logger;

    public HttpRequestBuilder builder() {
        return HttpRequestBuilder.builder()
                .restTemplate(restTemplate)
                .objectMapper(objectMapper)
                .logger(logger)
                .build();
    }

    @Builder
    @Getter
    @Setter
    public static class HttpRequestBuilder {

        private final RestTemplate restTemplate;
        private final ObjectMapper objectMapper;
        private final LoggingContext logger;

        @Builder.Default private String url = null;
        @Builder.Default private HttpMethod method = HttpMethod.GET;
        @Builder.Default private Map<String, String> headers = new HashMap<>();
        @Builder.Default private Map<String, String> queryParams = new HashMap<>();
        @Builder.Default private Object requestBody = null;
        @Builder.Default private Integer connectTimeout = null;
        @Builder.Default private Integer readTimeout = null;

        public HttpRequestBuilder url(String url) {
            this.url = url;
            return this;
        }

        public HttpRequestBuilder method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public HttpRequestBuilder header(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public HttpRequestBuilder addHeaders(Map<String, String> map) {
            if (map != null) this.headers.putAll(map);
            return this;
        }

        public HttpRequestBuilder queryParam(String key, String value) {
            this.queryParams.put(key, value);
            return this;
        }

        public HttpRequestBuilder addQueryParams(Map<String, String> params) {
            if (params != null) this.queryParams.putAll(params);
            return this;
        }

        public HttpRequestBuilder jsonBody(Object body) {
            try {
                this.requestBody = objectMapper.writeValueAsString(body);
                this.headers.put("Content-Type", "application/json");
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize JSON body", e);
            }
            return this;
        }

        public HttpRequestBuilder formBody(Map<String, String> formData) {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            if (formData != null) formData.forEach(form::add);
            this.requestBody = form;
            this.headers.put("Content-Type", "application/x-www-form-urlencoded");
            return this;
        }

        public HttpRequestBuilder multipartBody(Map<String, Object> parts) {
            MultiValueMap<String, Object> multipart = new LinkedMultiValueMap<>();
            if (parts != null) parts.forEach(multipart::add);
            this.requestBody = multipart;
            this.headers.put("Content-Type", "multipart/form-data");
            return this;
        }

        public HttpRequestBuilder connectTimeout(int timeoutMs) {
            this.connectTimeout = timeoutMs;
            return this;
        }

        public HttpRequestBuilder readTimeout(int timeoutMs) {
            this.readTimeout = timeoutMs;
            return this;
        }

        private RestTemplate getConfiguredClient() {
            if (connectTimeout == null && readTimeout == null) {
                return restTemplate;
            }
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            if (connectTimeout != null) factory.setConnectTimeout(connectTimeout);
            if (readTimeout != null) factory.setReadTimeout(readTimeout);
            RestTemplate custom = new RestTemplate(factory);
            custom.setMessageConverters(restTemplate.getMessageConverters());
            custom.setInterceptors(restTemplate.getInterceptors());

            return custom;
        }

        private void setupOutgoingApiLog(String url, int status, long latency, String message, Object responseBody) {
            if(message==null){
                message = "Response Received";
            }
            MDC.put("method", method.name());
            MDC.put("url", url);
            MDC.put("response_code", String.valueOf(status));
            MDC.put("latency_ms", String.valueOf(latency));


            if (WhiteListedKeys.checkIfWhiteListed("request_body")) {
                try {
                    String jsonRequest = objectMapper.writeValueAsString(requestBody);
                    MDC.put("request_body", WhiteListedKeys.filteredWhiteListedFields(jsonRequest));
                } catch (Exception e) {
                    MDC.put("request_body", "ERROR_CONVERTING_TO_JSON");
                }
            } else {
                MDC.put("request_body", "FILTERED");
            }


            if (WhiteListedKeys.checkIfWhiteListed("response_body")) {
                try {
                    String jsonResponse = objectMapper.writeValueAsString(responseBody);
                    MDC.put("response_body", WhiteListedKeys.filteredWhiteListedFields(jsonResponse));
                } catch (Exception e) {
                    MDC.put("response_body", "ERROR_CONVERTING_TO_JSON");
                }
            } else {
                MDC.put("response_body", "FILTERED");
            }


            MDC.put("category", "OUTGOING_API");
            logger.apiLog("OUTGOING_API_CALL", message);
            RequestLoggingContext.clear();
        }

        public <T> ApiResponse<T> execute(Class<T> responseType) {
            long start = System.currentTimeMillis();
            try {
                if (url == null) throw new IllegalStateException("URL must be set");

                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
                queryParams.forEach(builder::queryParam);

                HttpHeaders httpHeaders = new HttpHeaders();
                headers.forEach(httpHeaders::set);

                HttpEntity<?> requestEntity = new HttpEntity<>(requestBody, httpHeaders);

                ResponseEntity<byte[]> response = getConfiguredClient()
                        .exchange(builder.toUriString(), method, requestEntity, byte[].class);

                long latency = System.currentTimeMillis() - start;

                String bodyString = response.getBody() != null ? new String(response.getBody(), StandardCharsets.UTF_8) : "";
                T responseObject = null;
                if (responseType == String.class) {
                    responseObject = responseType.cast(bodyString);
                } else if (bodyString != null && !bodyString.isEmpty()) {
                    try {
                        responseObject = objectMapper.readValue(bodyString, responseType);
                    } catch (Exception e) {
                        setupOutgoingApiLog(url, 500, latency, "Failed to parse JSON response" + e.getMessage(), null);
                    }
                }

                setupOutgoingApiLog(url, response.getStatusCode().value(), latency, null, responseObject);

                return new ApiResponse(responseObject, response.getHeaders(), response.getStatusCode());

            } catch (HttpClientErrorException e) {
                long latency = System.currentTimeMillis() - start;
                setupOutgoingApiLog(url, e.getStatusCode().value(), latency, "HTTP Error: " + e.getMessage(), null);
                return new ApiResponse(null, e.getResponseHeaders(), e.getStatusCode());
            } catch (ResourceAccessException e) {
                long latency = System.currentTimeMillis() - start;
                setupOutgoingApiLog(url, 500, latency, "Timeout or connection error: " + e.getMessage(), null);
                return new ApiResponse(null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (Exception e) {
                long latency = System.currentTimeMillis() - start;
                setupOutgoingApiLog(url, 500, latency, "Error executing HTTP request: " + e.getMessage(), null);
                return new ApiResponse(null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

    }
}

