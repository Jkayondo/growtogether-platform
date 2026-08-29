package africa.growtogether.platform.eip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConnectorCertificationTest {

    @Test
    void newCertificationStartsPendingAndUncertified() {
        ConnectorCertification certification =
                new ConnectorCertification(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "production"
                );

        assertThat(certification.environment())
                .isEqualTo("PRODUCTION");

        assertThat(certification.status())
                .isEqualTo("PENDING");

        assertThat(certification.isCertified())
                .isFalse();

        assertThat(certification.hasCertificationTimestamp())
                .isFalse();
    }

    @Test
    void certifyMarksCertificationAsCertifiedWithTimestamp() {
        ConnectorCertification certification =
                newCertification();

        Instant expiresAt =
                Instant.now().plusSeconds(3600);

        certification.certify(
                "evidence://certification",
                expiresAt,
                "Provider certification passed"
        );

        assertThat(certification.status())
                .isEqualTo("CERTIFIED");

        assertThat(certification.isCertified())
                .isTrue();

        assertThat(certification.hasCertificationTimestamp())
                .isTrue();

        assertThat(certification.certifiedAt())
                .isNotNull();

        assertThat(certification.expiresAt())
                .isEqualTo(expiresAt);
    }

    @Test
    void futureCertificationIsNotExpired() {
        ConnectorCertification certification =
                newCertification();

        Instant checkTime = Instant.now();

        certification.certify(
                "evidence://future",
                checkTime.plusSeconds(3600),
                null
        );

        assertThat(
                certification.isExpiredAt(checkTime)
        ).isFalse();
    }

    @Test
    void expiredCertificationIsDetected() {
        ConnectorCertification certification =
                newCertification();

        Instant checkTime = Instant.now();

        certification.certify(
                "evidence://expired",
                checkTime.minusSeconds(1),
                null
        );

        assertThat(
                certification.isExpiredAt(checkTime)
        ).isTrue();
    }

    @Test
    void certificationWithoutExpiryDoesNotExpire() {
        ConnectorCertification certification =
                newCertification();

        Instant checkTime = Instant.now();

        certification.certify(
                "evidence://no-expiry",
                null,
                null
        );

        assertThat(
                certification.isExpiredAt(checkTime)
        ).isFalse();
    }

    @Test
    void failedCertificationIsNotCertified() {
        ConnectorCertification certification =
                newCertification();

        certification.fail(
                "Certification checks failed"
        );

        assertThat(certification.status())
                .isEqualTo("FAILED");

        assertThat(certification.isCertified())
                .isFalse();
    }

    @Test
    void expiryCheckRejectsNullInstant() {
        ConnectorCertification certification =
                newCertification();

        assertThatThrownBy(
                () -> certification.isExpiredAt(null)
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "instant must not be null"
                );
    }

    private ConnectorCertification newCertification() {
        return new ConnectorCertification(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "PRODUCTION"
        );
    }
}
