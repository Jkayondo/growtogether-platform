package africa.growtogether.platform.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gt.security.jwt")
public record JwtProperties(String issuer, String secret, long accessTokenSeconds) {}
