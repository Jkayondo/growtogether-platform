package africa.growtogether.platform.eiam.mfa;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("gt.security.mfa")
public record MfaProperties(String encryptionKey,int challengeSeconds,int trustedDeviceSeconds,int totpWindow){
 public MfaProperties{if(encryptionKey==null||encryptionKey.length()<32)throw new IllegalArgumentException("MFA encryption key must be at least 32 characters.");if(challengeSeconds<=0)challengeSeconds=300;if(trustedDeviceSeconds<=0)trustedDeviceSeconds=2592000;if(totpWindow<0)totpWindow=1;}
}
