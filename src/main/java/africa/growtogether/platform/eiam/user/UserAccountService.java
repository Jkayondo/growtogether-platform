package africa.growtogether.platform.eiam.user;

import africa.growtogether.platform.common.security.PasswordService;
import africa.growtogether.platform.common.web.RequestContextHolder;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountService {
    private final UserAccountRepository repository;
    private final PasswordService passwords;

    public UserAccountService(UserAccountRepository repository, PasswordService passwords) {
        this.repository = repository;
        this.passwords = passwords;
    }

    @Transactional
    public UserView create(CreateUserCommand command) {
        UUID tenantId = activeTenant();
        String username = normalize(command.username());
        String email = normalize(command.email());
        assertUnique(tenantId, null, username, email);
        UserAccount user = new UserAccount(username, email, command.displayName(), passwords.hash(command.password()));
        return UserView.from(repository.saveAndFlush(user));
    }

    @Transactional(readOnly = true)
    public UserView get(UUID id) {
        return UserView.from(requiredUser(id));
    }

    @Transactional(readOnly = true)
    public PageView<UserView> search(UserSearchCriteria criteria, int page, int size) {
        UUID tenantId = activeTenant();
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        PageRequest pageable = PageRequest.of(safePage, safeSize,
            Sort.by(Sort.Order.asc("displayName"), Sort.Order.asc("id")));
        Page<UserView> result = repository.findAll(specification(tenantId, criteria), pageable)
            .map(UserView::from);
        return PageView.from(result);
    }

    @Transactional
    public UserView update(UUID id, UpdateUserCommand command) {
        UUID tenantId = activeTenant();
        UserAccount user = requiredUser(id, tenantId);
        String username = normalize(command.username());
        String email = normalize(command.email());
        assertUnique(tenantId, id, username, email);
        user.updateProfile(username, email, command.displayName());
        return UserView.from(repository.saveAndFlush(user));
    }

    @Transactional
    public UserView activate(UUID id) {
        UserAccount user = requiredUser(id);
        user.activate();
        return UserView.from(repository.saveAndFlush(user));
    }

    @Transactional
    public UserView suspend(UUID id) {
        UserAccount user = requiredUser(id);
        user.suspend();
        return UserView.from(repository.saveAndFlush(user));
    }

    @Transactional
    public UserView deactivate(UUID id) {
        UserAccount user = requiredUser(id);
        user.deactivate();
        return UserView.from(repository.saveAndFlush(user));
    }

    private static Specification<UserAccount> specification(UUID tenantId, UserSearchCriteria criteria) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("tenantId"), tenantId));
            if (criteria.status() != null) {
                predicates.add(builder.equal(root.get("accountStatus"), criteria.status()));
            }
            String search = criteria.normalizedQuery();
            if (search != null) {
                String pattern = "%" + search + "%";
                predicates.add(builder.or(
                    builder.like(builder.lower(root.get("username")), pattern),
                    builder.like(builder.lower(root.get("email")), pattern),
                    builder.like(builder.lower(root.get("displayName")), pattern)));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void assertUnique(UUID tenantId, UUID currentUserId, String username, String email) {
        repository.findByTenantIdAndUsernameIgnoreCase(tenantId, username)
            .filter(existing -> !existing.getId().equals(currentUserId))
            .ifPresent(existing -> { throw new DuplicateUserException("username", "Username is already in use for this tenant."); });
        repository.findByTenantIdAndEmailIgnoreCase(tenantId, email)
            .filter(existing -> !existing.getId().equals(currentUserId))
            .ifPresent(existing -> { throw new DuplicateUserException("email", "Email is already in use for this tenant."); });
    }

    private UserAccount requiredUser(UUID id) {
        return requiredUser(id, activeTenant());
    }

    private UserAccount requiredUser(UUID id, UUID tenantId) {
        return repository.findByIdAndTenantId(id, tenantId).orElseThrow(UserNotFoundException::new);
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase();
    }

    private static UUID activeTenant() {
        return RequestContextHolder.current().map(context -> context.tenantId())
            .filter(value -> value != null && !value.isBlank()).map(UUID::fromString)
            .orElseThrow(() -> new IllegalStateException("An active tenant is required."));
    }
}
