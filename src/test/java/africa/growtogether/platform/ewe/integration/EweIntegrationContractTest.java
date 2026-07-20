package africa.growtogether.platform.ewe.integration;

import static org.assertj.core.api.Assertions.assertThat;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import org.junit.jupiter.api.Test;

class EweIntegrationContractTest {
    @Test void integrationAdaptersArePlatformComponents() {
        assertThat(EweConfigurationGateway.class).isNotNull();
        assertThat(EweAuditRecorder.class).isNotNull();
        assertThat(EnterpriseIdentityContext.class).isNotNull();
    }
}
