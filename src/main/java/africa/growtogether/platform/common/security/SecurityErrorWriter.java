package africa.growtogether.platform.common.security;

import africa.growtogether.platform.common.api.ApiMetadata;
import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.web.RequestContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public final class SecurityErrorWriter {
    private final ObjectMapper objectMapper;

    public SecurityErrorWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletResponse response, int status, String code, String message) throws IOException {
        if (response.isCommitted()) return;
        var context = RequestContextHolder.current().orElse(null);
        String correlationId = context == null ? null : context.correlationId();
        String tenantId = context == null ? null : context.tenantId();
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.failure(
            code, message, List.of(), new ApiMetadata(correlationId, tenantId, Instant.now())));
    }
}
