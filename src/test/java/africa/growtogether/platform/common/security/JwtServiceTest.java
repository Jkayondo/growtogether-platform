package africa.growtogether.platform.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;

class JwtServiceTest {
    @Test
    void issuesTokenContainingGrowTogetherPrincipalClaims() {
        JwtProperties properties = new JwtProperties("gt-test", "01234567890123456789012345678901", 300);
        var configuration = new SecurityConfiguration();
        var key = configuration.jwtSecretKey(properties);
        JwtService service = new JwtService(configuration.jwtEncoder(key), properties);
        JwtDecoder decoder = configuration.jwtDecoder(key);
        GtPrincipal principal = new GtPrincipal(UUID.randomUUID(), "john", UUID.randomUUID(),
            Set.of("ADMIN"), Set.of("eiam.users.create"), UUID.randomUUID());

        var jwt = decoder.decode(service.issueAccessToken(principal));

        assertThat(jwt.getSubject()).isEqualTo(principal.userId().toString());
        assertThat(jwt.getClaimAsString("tenant_id")).isEqualTo(principal.tenantId().toString());
        assertThat(jwt.getClaimAsStringList("permissions")).contains("eiam.users.create");
    }
}
