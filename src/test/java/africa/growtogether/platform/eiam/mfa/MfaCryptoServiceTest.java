package africa.growtogether.platform.eiam.mfa;
import static org.assertj.core.api.Assertions.assertThat; import org.junit.jupiter.api.Test;
class MfaCryptoServiceTest {@Test void encryptsWithRandomizedAuthenticatedEncryption(){MfaCryptoService service=new MfaCryptoService(new MfaProperties("01234567890123456789012345678901",300,2592000,1));String first=service.encrypt("SECRET"),second=service.encrypt("SECRET");assertThat(first).isNotEqualTo(second);assertThat(service.decrypt(first)).isEqualTo("SECRET");assertThat(service.decrypt(second)).isEqualTo("SECRET");}}
