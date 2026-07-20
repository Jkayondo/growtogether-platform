package africa.growtogether.platform.eiam.auth;
import java.time.Instant; import java.util.Set; import java.util.UUID;
public record TokenResponse(String tokenType,String accessToken,Instant accessTokenExpiresAt,String refreshToken,Instant refreshTokenExpiresAt,UUID sessionId,UUID userId,UUID tenantId,String username,Set<String> roles,Set<String> permissions) {}
