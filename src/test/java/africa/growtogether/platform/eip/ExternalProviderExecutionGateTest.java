package africa.growtogether.platform.eip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.eip.integration.EipConfigurationGateway;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExternalProviderExecutionGateTest {

    private ExternalConnectorReadGateway connectors;
    private ConnectorCertificationRepository certifications;
    private EipConfigurationGateway configuration;
    private ExternalProviderExecutionGate gate;

    private UUID tenantId;
    private UUID connectorId;

    @BeforeEach
    void setUp() {
        connectors = mock(ExternalConnectorReadGateway.class);
        certifications = mock(ConnectorCertificationRepository.class);
        configuration = mock(EipConfigurationGateway.class);

        gate = new ExternalProviderExecutionGate(
                connectors,
                certifications,
                configuration
        );

        tenantId = UUID.randomUUID();
        connectorId = UUID.randomUUID();
    }

    @Test
    void rejectsWhenExternalDeliveryIsDisabled() {
        when(configuration.externalDeliveryEnabled(tenantId))
                .thenReturn(false);

        assertThatThrownBy(
                () -> gate.authorize(tenantId, connectorId)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("External provider delivery is disabled");

        verify(connectors, never())
                .require(tenantId, connectorId);
    }

    @Test
    void rejectsInactiveConnector() {
        when(configuration.externalDeliveryEnabled(tenantId))
                .thenReturn(true);

        when(connectors.require(tenantId, connectorId))
                .thenReturn(
                        new ExternalConnectorSnapshot(
                                connectorId,
                                "WHATSAPP_PRIMARY",
                                "WHATSAPP",
                                false,
                                "ACTIVE"
                        )
                );

        assertThatThrownBy(
                () -> gate.authorize(tenantId, connectorId)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("External connector is not active");
    }

    @Test
    void rejectsWhenCertificationIsMissingForEnvironment() {
        usableConnector();
        when(configuration.executionEnvironment(tenantId))
                .thenReturn("STAGING");

        when(
                certifications
                        .findByTenantIdAndConnectorIdAndEnvironmentIgnoreCase(
                                tenantId,
                                connectorId,
                                "STAGING"
                        )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> gate.authorize(tenantId, connectorId)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "External connector is not certified for STAGING"
                );
    }

    @Test
    void rejectsInactiveCertificationRecord() {
        usableConnector();
        when(configuration.executionEnvironment(tenantId))
                .thenReturn("PRODUCTION");

        ConnectorCertification certification =
                certification(
                        EntityStatus.INACTIVE,
                        "CERTIFIED",
                        Instant.now(),
                        Instant.now().plusSeconds(3600)
                );

        certificationFor(
                "PRODUCTION",
                certification
        );

        assertThatThrownBy(
                () -> gate.authorize(tenantId, connectorId)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Connector certification record is not active"
                );
    }

    @Test
    void rejectsCertificationThatIsNotCertified() {
        usableConnector();
        when(configuration.executionEnvironment(tenantId))
                .thenReturn("PRODUCTION");

        ConnectorCertification certification =
                certification(
                        EntityStatus.ACTIVE,
                        "FAILED",
                        Instant.now(),
                        null
                );

        certificationFor(
                "PRODUCTION",
                certification
        );

        assertThatThrownBy(
                () -> gate.authorize(tenantId, connectorId)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "External connector certification is not CERTIFIED"
                );
    }

    @Test
    void rejectsCertificationWithoutCertificationTimestamp() {
        usableConnector();
        when(configuration.executionEnvironment(tenantId))
                .thenReturn("PRODUCTION");

        ConnectorCertification certification =
                certification(
                        EntityStatus.ACTIVE,
                        "CERTIFIED",
                        null,
                        Instant.now().plusSeconds(3600)
                );

        certificationFor(
                "PRODUCTION",
                certification
        );

        assertThatThrownBy(
                () -> gate.authorize(tenantId, connectorId)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "External connector certification has no certification timestamp"
                );
    }

    @Test
    void rejectsExpiredCertification() {
        usableConnector();
        when(configuration.executionEnvironment(tenantId))
                .thenReturn("PRODUCTION");

        ConnectorCertification certification =
                certification(
                        EntityStatus.ACTIVE,
                        "CERTIFIED",
                        Instant.now().minusSeconds(7200),
                        Instant.now().minusSeconds(3600)
                );

        certificationFor(
                "PRODUCTION",
                certification
        );

        assertThatThrownBy(
                () -> gate.authorize(tenantId, connectorId)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "External connector certification has expired"
                );
    }

    @Test
    void authorizesUsableCertifiedConnector() {
        usableConnector();

        when(configuration.executionEnvironment(tenantId))
                .thenReturn("PRODUCTION");

        UUID certificationId = UUID.randomUUID();

        ConnectorCertification certification =
                mock(ConnectorCertification.class);

        when(certification.getStatus())
                .thenReturn(EntityStatus.ACTIVE);

        when(certification.isCertified())
                .thenReturn(true);

        when(certification.hasCertificationTimestamp())
                .thenReturn(true);

        when(
                certification.isExpiredAt(
                        org.mockito.ArgumentMatchers.any(Instant.class)
                )
        ).thenReturn(false);

        when(certification.id())
                .thenReturn(certificationId);

        certificationFor(
                "PRODUCTION",
                certification
        );

        ExternalProviderExecutionAuthorization authorization =
                gate.authorize(
                        tenantId,
                        connectorId
                );

        assertThat(authorization.connectorId())
                .isEqualTo(connectorId);

        assertThat(authorization.connectorCode())
                .isEqualTo("WHATSAPP_PRIMARY");

        assertThat(authorization.connectorType())
                .isEqualTo("WHATSAPP");

        assertThat(authorization.executionEnvironment())
                .isEqualTo("PRODUCTION");

        assertThat(authorization.certificationId())
                .isEqualTo(certificationId);
    }

    private void usableConnector() {
        when(configuration.externalDeliveryEnabled(tenantId))
                .thenReturn(true);

        when(connectors.require(tenantId, connectorId))
                .thenReturn(
                        new ExternalConnectorSnapshot(
                                connectorId,
                                "WHATSAPP_PRIMARY",
                                "WHATSAPP",
                                true,
                                "ACTIVE"
                        )
                );
    }

    private ConnectorCertification certification(
            EntityStatus entityStatus,
            String certificationStatus,
            Instant certifiedAt,
            Instant expiresAt
    ) {
        ConnectorCertification certification =
                mock(ConnectorCertification.class);

        when(certification.getStatus())
                .thenReturn(entityStatus);

        when(certification.isCertified())
                .thenReturn(
                        "CERTIFIED".equalsIgnoreCase(
                                certificationStatus
                        )
                );

        when(certification.hasCertificationTimestamp())
                .thenReturn(certifiedAt != null);

        when(
                certification.isExpiredAt(
                        org.mockito.ArgumentMatchers.any(Instant.class)
                )
        ).thenReturn(
                expiresAt != null
                        && !expiresAt.isAfter(Instant.now())
        );

        return certification;
    }

    private void certificationFor(
            String environment,
            ConnectorCertification certification
    ) {
        when(
                certifications
                        .findByTenantIdAndConnectorIdAndEnvironmentIgnoreCase(
                                tenantId,
                                connectorId,
                                environment
                        )
        ).thenReturn(Optional.of(certification));
    }
}
