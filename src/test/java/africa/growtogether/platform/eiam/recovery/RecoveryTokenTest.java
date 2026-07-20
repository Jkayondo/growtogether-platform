package africa.growtogether.platform.eiam.recovery;
import static org.junit.jupiter.api.Assertions.*; import java.time.*; import java.util.UUID; import org.junit.jupiter.api.Test;
class RecoveryTokenTest {
 @Test void tokenIsSingleUse(){Instant now=Instant.now(); RecoveryToken t=new RecoveryToken(UUID.randomUUID(),RecoveryTokenPurpose.PASSWORD_RESET,"a".repeat(64),now.plusSeconds(60)); assertTrue(t.usableAt(now)); t.consume(now); assertFalse(t.usableAt(now)); assertThrows(InvalidRecoveryTokenException.class,()->t.consume(now));}
 @Test void expiredTokenCannotBeConsumed(){Instant now=Instant.now(); RecoveryToken t=new RecoveryToken(UUID.randomUUID(),RecoveryTokenPurpose.EMAIL_VERIFICATION,"b".repeat(64),now.minusSeconds(1)); assertThrows(InvalidRecoveryTokenException.class,()->t.consume(now));}
}
