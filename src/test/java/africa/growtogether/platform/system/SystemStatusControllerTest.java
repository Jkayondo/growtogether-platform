package africa.growtogether.platform.system;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.error.GlobalExceptionHandler;
import africa.growtogether.platform.common.security.SecurityErrorWriter;
import africa.growtogether.platform.common.web.RequestContextFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SystemStatusController.class)
@Import({
    ApiResponses.class,
    RequestContextFilter.class,
    GlobalExceptionHandler.class
})
class SystemStatusControllerTest {

    @Autowired
    MockMvc mockMvc;

    /*
     * These security beans are mocked because this test verifies the
     * system-status endpoint rather than JWT authentication behaviour.
     */
    @MockitoBean
    JwtDecoder jwtDecoder;

    @MockitoBean
    SecurityErrorWriter securityErrorWriter;

    @Test
    void returnsStandardOperationalStatusResponse() throws Exception {
        mockMvc.perform(
                get("/api/v1/system/status")
                    .header(
                        "X-Correlation-ID",
                        "test-correlation-001"
                    )
            )
            .andExpect(status().isOk())
            .andExpect(
                header().string(
                    "X-Correlation-ID",
                    "test-correlation-001"
                )
            )
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(
                jsonPath("$.code")
                    .value("GT-SYSTEM-STATUS-OK")
            )
            .andExpect(
                jsonPath("$.data.service")
                    .value("gt-platform")
            )
            .andExpect(
                jsonPath("$.data.status")
                    .value("UP")
            )
            .andExpect(
                jsonPath(
                    "$.data.timestamp",
                    matchesPattern(
                        "^\\d{4}-\\d{2}-\\d{2}T.*Z$"
                    )
                )
            )
            .andExpect(
                jsonPath("$.metadata.correlationId")
                    .value("test-correlation-001")
            );
    }

    @Test
    void generatesCorrelationIdWhenClientDoesNotSupplyOne()
        throws Exception {

        mockMvc.perform(
                get("/api/v1/system/status")
            )
            .andExpect(status().isOk())
            .andExpect(
                header().string(
                    "X-Correlation-ID",
                    matchesPattern("^[0-9a-f-]{36}$")
                )
            )
            .andExpect(
                jsonPath(
                    "$.metadata.correlationId",
                    matchesPattern("^[0-9a-f-]{36}$")
                )
            );
    }

    @Test
    void returnsStandardFailureForMalformedTenantId()
        throws Exception {

        mockMvc.perform(
                get("/api/v1/system/status")
                    .header(
                        "X-Tenant-ID",
                        "not-a-uuid"
                    )
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(
                jsonPath("$.code")
                    .value("GT-REQUEST-001")
            )
            .andExpect(
                jsonPath("$.errors[0].field")
                    .value("X-Tenant-ID")
            );
    }

    @Test
    void propagatesValidTenantId() throws Exception {
        String tenantId =
            "b5fe3c82-2ff0-4da0-b6d3-373e9b76c928";

        mockMvc.perform(
                get("/api/v1/system/status")
                    .header(
                        "X-Tenant-ID",
                        tenantId
                    )
            )
            .andExpect(status().isOk())
            .andExpect(
                header().string(
                    "X-Tenant-ID",
                    tenantId
                )
            )
            .andExpect(
                jsonPath("$.metadata.tenantId")
                    .value(tenantId)
            );
    }
}