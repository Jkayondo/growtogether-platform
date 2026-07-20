package africa.growtogether.platform.common.web;

import africa.growtogether.platform.common.api.ApiMetadata;
import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.FieldViolation;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;

@Component("gtRequestContextFilter")
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class RequestContextFilter extends OncePerRequestFilter {
    private static final Pattern SAFE_CORRELATION_ID = Pattern.compile("^[A-Za-z0-9._:-]{1,128}$");

    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Autowired
    public RequestContextFilter(ObjectMapper objectMapper) {
        this(objectMapper, Clock.systemUTC());
    }

    RequestContextFilter(ObjectMapper objectMapper, Clock clock) {
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String correlationId = UUID.randomUUID().toString();

        try {
            correlationId = resolveCorrelationId(request.getHeader(RequestContext.CORRELATION_HEADER));
            response.setHeader(RequestContext.CORRELATION_HEADER, correlationId);
            String tenantId = resolveTenantId(request.getHeader(RequestContext.TENANT_HEADER));
            bindContext(correlationId, tenantId, response);
            filterChain.doFilter(request, response);
        } catch (InvalidRequestHeaderException exception) {
            writeInvalidHeaderResponse(response, correlationId, exception);
        } finally {
            RequestContextHolder.clear();
            MDC.remove("correlationId");
            MDC.remove("tenantId");
        }
    }

    private void bindContext(String correlationId, String tenantId, HttpServletResponse response) {
        RequestContextHolder.set(new RequestContext(correlationId, tenantId));
        MDC.put("correlationId", correlationId);
        putMdcIfPresent("tenantId", tenantId);
        if (tenantId != null) {
            response.setHeader(RequestContext.TENANT_HEADER, tenantId);
        }
    }

    private void writeInvalidHeaderResponse(
        HttpServletResponse response,
        String correlationId,
        InvalidRequestHeaderException exception
    ) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.resetBuffer();
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(RequestContext.CORRELATION_HEADER, correlationId);

        ApiResponse<Void> body = ApiResponse.failure(
            "GT-REQUEST-001",
            "Invalid request header.",
            List.of(new FieldViolation(exception.headerName(), exception.getMessage())),
            new ApiMetadata(correlationId, null, Instant.now(clock))
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    private static String resolveCorrelationId(String supplied) {
        if (supplied == null || supplied.isBlank()) {
            return UUID.randomUUID().toString();
        }
        String candidate = supplied.trim();
        if (!SAFE_CORRELATION_ID.matcher(candidate).matches()) {
            throw new InvalidRequestHeaderException(
                RequestContext.CORRELATION_HEADER,
                "Correlation ID must contain 1-128 letters, numbers, dots, underscores, colons, or hyphens."
            );
        }
        return candidate;
    }

    private static String resolveTenantId(String supplied) {
        if (supplied == null || supplied.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(supplied.trim()).toString();
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestHeaderException(
                RequestContext.TENANT_HEADER,
                "Tenant ID must be a valid UUID."
            );
        }
    }

    private static void putMdcIfPresent(String key, String value) {
        if (value != null) {
            MDC.put(key, value);
        }
    }
}
