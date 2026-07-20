package africa.growtogether.platform.common.security;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public final class JwtService {
    private final JwtEncoder encoder;
    private final JwtProperties properties;

    public JwtService(JwtEncoder encoder, JwtProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    public String issueAccessToken(GtPrincipal principal) { return issueAccessToken(principal, "AAL1"); }

    public String issueAccessToken(GtPrincipal principal, String assuranceLevel) {
        Instant issuedAt = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(properties.issuer())
            .issuedAt(issuedAt)
            .expiresAt(issuedAt.plusSeconds(properties.accessTokenSeconds()))
            .subject(principal.userId().toString())
            .claim("username", principal.username())
            .claim("tenant_id", principal.tenantId().toString())
            .claim("roles", List.copyOf(principal.roles()))
            .claim("permissions", List.copyOf(principal.permissions()))
            .claim("session_id", principal.sessionId().toString())
            .claim("acr", assuranceLevel)
            .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
