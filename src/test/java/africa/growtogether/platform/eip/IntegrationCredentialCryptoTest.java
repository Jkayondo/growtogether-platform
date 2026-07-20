package africa.growtogether.platform.eip;
import static org.junit.jupiter.api.Assertions.*; import java.util.Base64; import org.junit.jupiter.api.Test;
class IntegrationCredentialCryptoTest {@Test void encryptsWithRandomIv(){String key=Base64.getEncoder().encodeToString(new byte[32]);IntegrationCredentialCrypto c=new IntegrationCredentialCrypto(key,"test");String a=c.encrypt("secret");String b=c.encrypt("secret");assertNotEquals(a,b);assertFalse(a.contains("secret"));}}
