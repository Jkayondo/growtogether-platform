package africa.growtogether.platform.eiam.mfa;
import static org.assertj.core.api.Assertions.assertThat; import java.time.Instant; import org.junit.jupiter.api.Test;
class TotpServiceTest {
 private final TotpService service=new TotpService(new MfaProperties("01234567890123456789012345678901",300,2592000,0));
 @Test void acceptsCurrentCodeAndRejectsMalformedCode(){String secret="JBSWY3DPEHPK3PXP";Instant now=Instant.ofEpochSecond(1_700_000_000L);String code=service.generate(secret,now.getEpochSecond()/30);assertThat(service.verify(secret,code,now)).isTrue();assertThat(service.verify(secret,"12AB56",now)).isFalse();}
 @Test void generatedSecretsHaveStrongBase32Entropy(){assertThat(service.generateSecret()).matches("[A-Z2-7]{32}");}
}
