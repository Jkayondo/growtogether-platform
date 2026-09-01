package africa.growtogether.platform.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class SecurityConfigurationCorsTest {

    @Test
    void permitsSupportedLocalDevelopmentOriginsAndRejectsUnknownOrigin() {

        SecurityConfiguration security =
            new SecurityConfiguration();

        CorsConfigurationSource source =
            security.corsConfigurationSource();

        MockHttpServletRequest request =
            new MockHttpServletRequest(
                "OPTIONS",
                "/api/v1/eiam/auth/login"
            );

        CorsConfiguration cors =
            source.getCorsConfiguration(
                request
            );

        assertNotNull(
            cors
        );

        assertEquals(
            "http://localhost:5173",
            cors.checkOrigin(
                "http://localhost:5173"
            )
        );

        assertEquals(
            "http://localhost:5174",
            cors.checkOrigin(
                "http://localhost:5174"
            )
        );

        assertNull(
            cors.checkOrigin(
                "https://untrusted.example"
            )
        );
    }
}
