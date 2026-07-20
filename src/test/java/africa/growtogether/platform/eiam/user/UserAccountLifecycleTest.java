package africa.growtogether.platform.eiam.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
}
