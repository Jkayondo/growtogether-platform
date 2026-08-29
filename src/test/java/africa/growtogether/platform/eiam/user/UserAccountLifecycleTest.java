package africa.growtogether.platform.eiam.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class UserAccountLifecycleTest {
    private UserAccount pendingUser() {
        return new UserAccount("john", "john@example.com", "John", "hash");
    }

    @Test
    void activatesPendingAccount() {
        UserAccount user = pendingUser();
        user.activate();
        assertThat(user.getAccountStatus()).isEqualTo(UserAccountStatus.ACTIVE);
    }

    @Test
    void suspendsOnlyActiveAccount() {
        UserAccount user = pendingUser();
        assertThatThrownBy(user::suspend).isInstanceOf(UserLifecycleException.class);
        user.activate();
        user.suspend();
        assertThat(user.getAccountStatus()).isEqualTo(UserAccountStatus.SUSPENDED);
    }

    @Test
    void deactivationIsTerminal() {
        UserAccount user = pendingUser();
        user.deactivate();
        assertThat(user.getAccountStatus()).isEqualTo(UserAccountStatus.DEACTIVATED);
        assertThatThrownBy(user::activate).isInstanceOf(UserLifecycleException.class);
        assertThatThrownBy(() -> user.updateProfile("new", "new@example.com", "New Name"))
            .isInstanceOf(UserLifecycleException.class);
    }

    @Test
    void lifecycleCommandsAreIdempotentAtTargetState() {
        UserAccount user = pendingUser();
        user.activate();
        user.activate();
        user.suspend();
        user.suspend();
        user.deactivate();
        user.deactivate();
        assertThat(user.getAccountStatus()).isEqualTo(UserAccountStatus.DEACTIVATED);
    }
    @Test
    void createsPhoneOnlyAccount() {
        UserAccount user =
            new UserAccount(
                "parent",
                null,
                "+256701234567",
                "Parent User",
                "hash"
            );

        assertThat(user.getEmail()).isNull();
        assertThat(user.getPrimaryPhoneNumber())
            .isEqualTo("+256701234567");
        assertThat(user.isPhoneVerified()).isFalse();
    }

    @Test
    void rejectsNonCanonicalPhoneIdentity() {
        assertThatThrownBy(
            () -> new UserAccount(
                "parent",
                null,
                "0701234567",
                "Parent User",
                "hash"
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(
                "canonical international format"
            );
    }

    @Test
    void verifiesPhoneIdentity() {
        UserAccount user =
            new UserAccount(
                "parent",
                null,
                "+256701234567",
                "Parent User",
                "hash"
            );

        Instant verifiedAt =
            Instant.parse("2026-08-22T20:00:00Z");

        user.verifyPhone(verifiedAt);

        assertThat(user.isPhoneVerified()).isTrue();
        assertThat(user.getPhoneVerifiedAt())
            .isEqualTo(verifiedAt);
    }

    @Test
    void cannotVerifyAbsentPhoneIdentity() {
        UserAccount user = pendingUser();

        assertThatThrownBy(
            () -> user.verifyPhone(
                Instant.parse("2026-08-22T20:00:00Z")
            )
        )
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(
                "without a phone number"
            );
    }

    @Test
    void changingPhoneClearsPreviousVerification() {
        UserAccount user =
            new UserAccount(
                "parent",
                null,
                "+256701234567",
                "Parent User",
                "hash"
            );

        user.verifyPhone(
            Instant.parse("2026-08-22T20:00:00Z")
        );

        user.updateProfile(
            "parent",
            null,
            "+256702345678",
            "Parent User"
        );

        assertThat(user.getPrimaryPhoneNumber())
            .isEqualTo("+256702345678");
        assertThat(user.isPhoneVerified()).isFalse();
        assertThat(user.getPhoneVerifiedAt()).isNull();
    }

    @Test
    void preservesExistingEmailOnlyIdentity() {
        UserAccount user =
            new UserAccount(
                "john",
                "John@Example.COM",
                "John",
                "hash"
            );

        assertThat(user.getEmail())
            .isEqualTo("john@example.com");
        assertThat(user.getPrimaryPhoneNumber()).isNull();
    }

}
