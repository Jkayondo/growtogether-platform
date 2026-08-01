package africa.growtogether.platform.eiam.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import africa.growtogether.platform.common.security.PasswordService;
import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@Transactional
class UserAccountIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.data.redis.repositories.enabled", () -> "false");
    }
    @Autowired UserAccountService service;
    @Autowired UserAccountRepository repository;
    @Autowired PasswordService passwords;

    @AfterEach void clear() { RequestContextHolder.clear(); }

    @Test
    void createsTenantScopedUserWithHashedPassword() {
        UUID tenant = UUID.randomUUID();
        RequestContextHolder.set(new RequestContext("test", tenant.toString()));
        UserView created = service.create(new CreateUserCommand("John.K", "John@Example.com", "John Kayondo", "strong-password-123"));
        UserAccount stored = repository.findById(created.id()).orElseThrow();
        assertThat(stored.getTenantId()).isEqualTo(tenant);
        assertThat(stored.getUsername()).isEqualTo("john.k");
        assertThat(stored.getEmail()).isEqualTo("john@example.com");
        assertThat(stored.getPasswordHash()).doesNotContain("strong-password-123");
        assertThat(passwords.matches("strong-password-123", stored.getPasswordHash())).isTrue();
    }

    @Test
    void preventsDuplicateUsernameWithinTenant() {
        RequestContextHolder.set(new RequestContext("test", UUID.randomUUID().toString()));
        var command = new CreateUserCommand("john", "one@example.com", "John One", "strong-password-123");
        service.create(command);
        assertThatThrownBy(() -> service.create(new CreateUserCommand("JOHN", "two@example.com", "John Two", "strong-password-456")))
            .isInstanceOf(DuplicateUserException.class);
    }
    @Test
    void updatesProfileWithinTenantAndPreservesPassword() {
        UUID tenant = UUID.randomUUID();
        RequestContextHolder.set(new RequestContext("test", tenant.toString()));
        UserView created = service.create(new CreateUserCommand("john", "john@example.com", "John", "strong-password-123"));
        String originalHash = repository.findById(created.id()).orElseThrow().getPasswordHash();

        UserView updated = service.update(created.id(), new UpdateUserCommand("john.k", "john.k@example.com", "John Kayondo"));

        assertThat(updated.username()).isEqualTo("john.k");
        assertThat(updated.email()).isEqualTo("john.k@example.com");
        assertThat(updated.displayName()).isEqualTo("John Kayondo");
        assertThat(repository.findById(created.id()).orElseThrow().getPasswordHash()).isEqualTo(originalHash);
    }

    @Test
    void appliesControlledLifecycleTransitions() {
        UUID tenant = UUID.randomUUID();
        RequestContextHolder.set(new RequestContext("test", tenant.toString()));
        UserView created = service.create(new CreateUserCommand("john", "john@example.com", "John", "strong-password-123"));

        assertThat(service.activate(created.id()).accountStatus()).isEqualTo(UserAccountStatus.ACTIVE);
        assertThat(service.suspend(created.id()).accountStatus()).isEqualTo(UserAccountStatus.SUSPENDED);
        assertThat(service.activate(created.id()).accountStatus()).isEqualTo(UserAccountStatus.ACTIVE);
        assertThat(service.deactivate(created.id()).accountStatus()).isEqualTo(UserAccountStatus.DEACTIVATED);
        assertThatThrownBy(() -> service.activate(created.id())).isInstanceOf(UserLifecycleException.class);
    }

    @Test
    void preventsUpdateFromClaimingAnotherUsersIdentity() {
        UUID tenant = UUID.randomUUID();
        RequestContextHolder.set(new RequestContext("test", tenant.toString()));
        UserView first = service.create(new CreateUserCommand("john", "john@example.com", "John", "strong-password-123"));
        service.create(new CreateUserCommand("mary", "mary@example.com", "Mary", "strong-password-456"));

        assertThatThrownBy(() -> service.update(first.id(),
            new UpdateUserCommand("mary", "john.new@example.com", "John")))
            .isInstanceOf(DuplicateUserException.class);
    }

    @Test
    void cannotManageUserFromAnotherTenant() {
        UUID firstTenant = UUID.randomUUID();
        RequestContextHolder.set(new RequestContext("test", firstTenant.toString()));
        UserView created = service.create(new CreateUserCommand("john", "john@example.com", "John", "strong-password-123"));

        RequestContextHolder.set(new RequestContext("test", UUID.randomUUID().toString()));
        assertThatThrownBy(() -> service.activate(created.id())).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void searchesOnlyWithinActiveTenantAndSupportsStatusFiltering() {
        UUID tenant = UUID.randomUUID();
        RequestContextHolder.set(new RequestContext("test", tenant.toString()));
        UserView john = service.create(new CreateUserCommand("john", "john@example.com", "John Kayondo", "strong-password-123"));
        service.create(new CreateUserCommand("mary", "mary@example.com", "Mary N.", "strong-password-456"));
        service.activate(john.id());

        UUID otherTenant = UUID.randomUUID();
        RequestContextHolder.set(new RequestContext("test", otherTenant.toString()));
        service.create(new CreateUserCommand("john.other", "john.other@example.com", "John Other", "strong-password-789"));

        RequestContextHolder.set(new RequestContext("test", tenant.toString()));
        PageView<UserView> result = service.search(new UserSearchCriteria("john", UserAccountStatus.ACTIVE), 0, 20);

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.content()).extracting(UserView::username).containsExactly("john");
    }

    @Test
    void paginatesUsersUsingStableDisplayNameOrdering() {
        RequestContextHolder.set(new RequestContext("test", UUID.randomUUID().toString()));
        service.create(new CreateUserCommand("zulu", "zulu@example.com", "Zulu User", "strong-password-123"));
        service.create(new CreateUserCommand("alpha", "alpha@example.com", "Alpha User", "strong-password-456"));
        service.create(new CreateUserCommand("mike", "mike@example.com", "Mike User", "strong-password-789"));

        PageView<UserView> firstPage = service.search(new UserSearchCriteria(null, null), 0, 2);
        PageView<UserView> secondPage = service.search(new UserSearchCriteria(null, null), 1, 2);

        assertThat(firstPage.content()).extracting(UserView::displayName)
            .containsExactly("Alpha User", "Mike User");
        assertThat(firstPage.totalElements()).isEqualTo(3);
        assertThat(firstPage.totalPages()).isEqualTo(2);
        assertThat(secondPage.content()).extracting(UserView::displayName).containsExactly("Zulu User");
    }

}
