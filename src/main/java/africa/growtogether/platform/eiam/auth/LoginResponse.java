package africa.growtogether.platform.eiam.auth;
import java.time.Instant;
public record LoginResponse(boolean mfaRequired,String challengeToken,Instant challengeExpiresAt,TokenResponse tokens,String trustedDeviceToken,Instant trustedDeviceExpiresAt){public static LoginResponse authenticated(TokenResponse t,String deviceToken,Instant deviceExpiry){return new LoginResponse(false,null,null,t,deviceToken,deviceExpiry);}public static LoginResponse challenge(String token,Instant expiry){return new LoginResponse(true,token,expiry,null,null,null);}}
