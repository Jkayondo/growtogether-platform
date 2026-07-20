package africa.growtogether.platform.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestContextFilterTest {
    private final RequestContextFilter filter = new RequestContextFilter(new ObjectMapper().findAndRegisterModules());

    @Test
    void makesContextAvailableDuringRequestAndClearsItAfterwards() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestContext.CORRELATION_HEADER, "corr-123");
        request.addHeader(RequestContext.TENANT_HEADER, "b5fe3c82-2ff0-4da0-b6d3-373e9b76c928");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<RequestContext> captured = new AtomicReference<>();

        filter.doFilter(request, response, (req, res) -> captured.set(RequestContextHolder.require()));

        assertThat(captured.get().correlationId()).isEqualTo("corr-123");
        assertThat(captured.get().tenantId()).isEqualTo("b5fe3c82-2ff0-4da0-b6d3-373e9b76c928");
        assertThat(RequestContextHolder.current()).isEmpty();
    }

    @Test
    void rejectsMalformedTenantIdWithStandardError() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestContext.TENANT_HEADER, "not-a-uuid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {});

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains("GT-REQUEST-001");
        assertThat(response.getContentAsString()).contains("X-Tenant-ID");
        assertThat(RequestContextHolder.current()).isEmpty();
    }
}
