package africa.growtogether.platform.eiam.auth;
import static org.junit.jupiter.api.Assertions.*; import org.junit.jupiter.api.Test;
class RefreshTokenServiceTest { private final RefreshTokenService service=new RefreshTokenService();
 @Test void generatesDistinctOpaqueTokensAndStableHashes(){String a=service.generate(),b=service.generate();assertNotEquals(a,b);assertEquals(64,service.hash(a).length());assertEquals(service.hash(a),service.hash(a));assertNotEquals(service.hash(a),service.hash(b));}
}
