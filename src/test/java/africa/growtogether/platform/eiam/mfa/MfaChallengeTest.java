package africa.growtogether.platform.eiam.mfa;
import static org.assertj.core.api.Assertions.assertThat; import java.time.*; import java.util.UUID; import org.junit.jupiter.api.Test;
class MfaChallengeTest {@Test void challengeIsSingleUseAndExpires(){Instant now=Instant.now();MfaChallenge c=new MfaChallenge(UUID.randomUUID(),"hash",now.plusSeconds(30));assertThat(c.consume(now)).isTrue();assertThat(c.consume(now.plusSeconds(1))).isFalse();MfaChallenge expired=new MfaChallenge(UUID.randomUUID(),"hash2",now.minusSeconds(1));assertThat(expired.consume(now)).isFalse();}}
