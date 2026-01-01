package devnox.library.logging;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.Arrays;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiLoggingFilter extends OncePerRequestFilter {

    @Autowired
    LoggingContext logger;

    @Autowired
    GlobalLoggingContext globalLoggingContext;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final ThreadLocal<Integer> callDepth = ThreadLocal.withInitial(() -> 0);

    private boolean isValidJson(String body) {
        if (body == null) {
            return false;
        }
        try {
            objectMapper.readTree(body);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean shouldWrapForLogging(HttpServletRequest req) {
        // Skip internal forwards/dispatches — you already have JSP checks & callDepth, keep them.
        if (req.isAsyncStarted()) return false;

        // Skip methods not carrying bodies or where bodies are pointless
        String method = req.getMethod();
        if ("HEAD".equalsIgnoreCase(method)) return false;

        // Skip known binary routes or attachments
        String accept = req.getHeader("Accept");
        if (accept != null && (
                accept.contains("application/octet-stream") ||
                accept.contains("application/zip") ||
                accept.contains("image/") ||
                accept.contains("video/") ||
                accept.contains("audio/"))) return false;

        // Skip range requests (downloads/resume)
        if (req.getHeader("Range") != null) return false;

        // If you know ahead of time the route is a download or redirect, skip via your blacklist.
        return true;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        int depth = callDepth.get();
        callDepth.set(depth + 1);

        try {
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();


            // Detect internal calls (JSP calls from your controller)
            boolean isInternalCall = uri.contains("redirects2s.jsp") || uri.contains("iframe.jsp");

            // Generate a new UUID
            String xRequestId = UUID.randomUUID().toString();;

            RequestLoggingContext.clear();

            MDC.put("x-request-id", xRequestId);
            globalLoggingContext.applyToMdc();

            long start = System.currentTimeMillis();

            if (!shouldWrapForLogging(request) || WhiteListedKeys.checkIfBlackListedRoute(uri, queryString) || isInternalCall || depth > 1) {
                handleSimpleRequest(request, response, filterChain, xRequestId, start);
            } else {
                handleFullLoggingRequest(request, response, filterChain, xRequestId, start);
            }
        } finally {
            // Always clean up call depth
            int currentDepth = callDepth.get();
            if (currentDepth > 1) {
                callDepth.set(currentDepth - 1);
            } else {
                callDepth.remove();
            }
        }
    }

    private void handleSimpleRequest(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain,
                                    String xRequestId,
                                    long start) throws IOException, ServletException {
        try {
            response.addHeader("x-session-id", xRequestId);
            // response.addCookie(new Cookie("x-session-id", xRequestId));
            filterChain.doFilter(request, response);
        } finally {
            String qs = request.getQueryString() != null ? request.getQueryString() : "";
            long latency = System.currentTimeMillis() - start;
            int status = response.getStatus();
            String url = request.getRequestURL().toString();
            
            if (WhiteListedKeys.checkIfWhiteListed("query_string")) {
                MDC.put("query_string", qs);
            } else {
                MDC.put("query_string", "FILTERED");
            }
            MDC.put("method", request.getMethod());
            MDC.put("url", url);
            MDC.put("response_code", String.valueOf(status));
            MDC.put("latency_ms", String.valueOf(latency));
            MDC.put("request_body", "SKIPPED_REQUEST");
            MDC.put("response_body", "SKIPPED_RESPONSE");
            MDC.put("request_headers", getHeadersMap(request).toString());
            MDC.put("response_headers", getHeadersMap(response).toString());
            MDC.put("category", "INCOMING_API");

            logger.apiLog("INCOMING_API_CALL", "Api log without Req/Res");
            RequestLoggingContext.clear();
        }
    }

    private void handleFullLoggingRequest(HttpServletRequest request,
                                         HttpServletResponse response,
                                         FilterChain filterChain,
                                         String xRequestId,
                                         long start) throws IOException, ServletException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        wrappedResponse.addHeader("x-session-id", xRequestId);
        // wrappedResponse.addCookie(new Cookie("x-session-id", xRequestId));

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long latency = System.currentTimeMillis() - start;

            // Request details
            String method = wrappedRequest.getMethod();
            String url = wrappedRequest.getRequestURL().toString();
            String qs = wrappedRequest.getQueryString() != null ? wrappedRequest.getQueryString() : "";
            Map<String, String> requestHeaders = getHeadersMap(wrappedRequest);

            // Response details
            int status = wrappedResponse.getStatus();
            Map<String, String> responseHeaders = getHeadersMap(wrappedResponse);

            MDC.put("method", method);
            MDC.put("url", url);
            MDC.put("response_code", String.valueOf(status));
            MDC.put("latency_ms", String.valueOf(latency));
            MDC.put("category", "INCOMING_API");

            if (WhiteListedKeys.checkIfWhiteListed("query_string")) {
                MDC.put("query_string", qs);
            } else {
                MDC.put("query_string", "FILTERED");
            }

            if (WhiteListedKeys.checkIfWhiteListed("request_headers")) {
                MDC.put("request_headers", WhiteListedKeys.filteredWhiteListedFields(requestHeaders.toString()));
            } else {
                MDC.put("request_headers", "FILTERED");
            }

            if (WhiteListedKeys.checkIfWhiteListed("request_body")) {
                String requestBody = getRequestBodyForLogging(wrappedRequest);
                MDC.put("request_body", WhiteListedKeys.filteredWhiteListedFields(requestBody));
            } else {
                MDC.put("request_body", "FILTERED");
            }

            if (WhiteListedKeys.checkIfWhiteListed("response_body")) {
                String responseBody = getResponseBodyForLogging(wrappedResponse);
                MDC.put("response_body", WhiteListedKeys.filteredWhiteListedFields(responseBody));
            } else {
                MDC.put("response_body", "FILTERED");
            }

            if (WhiteListedKeys.checkIfWhiteListed("client_ip")) {
                MDC.put("client_ip", request.getRemoteAddr());
            } else {
                MDC.put("client_ip", "FILTERED");
            }

            if (WhiteListedKeys.checkIfWhiteListed("response_headers")) {
                MDC.put("response_headers", WhiteListedKeys.filteredWhiteListedFields(responseHeaders.toString()));
            } else {
                MDC.put("response_headers", "FILTERED");
            }

            logger.apiLog("INCOMING_API_CALL", "Full Api Response with Req/Res body");
            RequestLoggingContext.clear();

            wrappedResponse.copyBodyToResponse(); // ✅ ensures original response body is sent
        }
    }

    private Map<String, String> getHeadersMap(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.put(headerName, request.getHeader(headerName));
            }
        }
        return headers;
    }

    private Map<String, String> getHeadersMap(HttpServletResponse response) {
        Map<String, String> headers = new LinkedHashMap<>();
        for (String headerName : response.getHeaderNames()) {
            headers.put(headerName, response.getHeader(headerName));
        }
        return headers;
    }

    private String getRequestBodyForLogging(ContentCachingRequestWrapper request) {
        try {
            byte[] buf = request.getContentAsByteArray();
            if (buf.length > 0) {
                return new String(buf, StandardCharsets.UTF_8);
            }
            return "";
        } catch (Exception e) {
            return "ERROR_READING_REQUEST/" + e.getMessage();
        }
    }

    private String getResponseBodyForLogging(ContentCachingResponseWrapper response) {
        try {
            String contentType = response.getContentType();
            byte[] buf = response.getContentAsByteArray();
            
            if (buf.length == 0) {
                return "[EMPTY_RESPONSE]";
            }

            // Check content type to determine logging strategy
            if (contentType != null) {
                contentType = contentType.toLowerCase();
                if (contentType.contains("text/css") ||
                    contentType.contains("javascript") ||
                    contentType.contains("html") ||
                    contentType.contains("xml") ||
                    contentType.contains("image") ||
                    contentType.contains("png") ||
                    contentType.contains("svg") ||
                    contentType.contains("zip") || 
                    contentType.contains("octet-stream") || 
                    contentType.contains("multipart")) {
                    return "[FILTERED : " + contentType + ", Size: " + buf.length + " bytes]";
                }
            }

            String respBody = new String(buf, StandardCharsets.UTF_8);
            
            if (isValidJson(respBody)) {
                return LoggingContext.truncateIfLong(respBody);
            } else {
                return "FILTERED/PLAINTEXT";
            }
        } catch (Exception e) {
            return "ERROR_READING_RESPONSE/" + e.getMessage();
        }
    }
}