package africa.growtogether.platform.eiam.auth;
import java.nio.charset.StandardCharsets; import java.security.*; import java.util.Base64; import org.springframework.stereotype.Service;
@Service public class RefreshTokenService {
 private final SecureRandom random=new SecureRandom();
 public String generate(){byte[] b=new byte[48];random.nextBytes(b);return Base64.getUrlEncoder().withoutPadding().encodeToString(b);}
 public String hash(String token){try{return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
}
