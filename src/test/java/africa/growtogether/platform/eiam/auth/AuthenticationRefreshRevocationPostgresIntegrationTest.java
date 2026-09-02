package africa.growtogether.platform.eiam.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.eiam.tenant.ProvisionTenantCommand;
import africa.growtogether.platform.eiam.tenant.TenantProvisioningService;
import africa.growtogether.platform.eiam.tenant.TenantView;
import africa.growtogether.platform.eiam.user.UserAccount;
import africa.growtogether.platform.eiam.user.UserAccountRepository;
import java.sql.Timestamp;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
class AuthenticationRefreshRevocationPostgresIntegrationTest {

    private static final String PASSWORD =
            "Strong-Test-Password-2026!";

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_refresh_revocation_test"
                    )
                    .withUsername(
                            "growtogether"
                    )
                    .withPassword(
                            "growtogether"
                    );

    @DynamicPropertySource
    static void properties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );

        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );

        registry.add(
                "spring.flyway.enabled",
                () -> "true"
        );

        registry.add(
                "spring.data.redis.repositories.enabled",
                () -> "false"
        );
    }

    @Autowired
    private TenantProvisioningService tenants;

    @Autowired
    private AuthenticationService authentication;

    @Autowired
    private UserAccountRepository users;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void clearContext() {
        RequestContextHolder.clear();
    }

    @Test
    void accountUnavailableRefreshRevocationMustSurviveRejectedRefresh() {
        TenantView tenant =
                tenants.provision(
                        new ProvisionTenantCommand(
                                "GT-AUTH-REFRESH-REV-ORG",
                                "GT Auth Refresh Revocation Organisation",
                                "GT-AUTH-REFRESH-REV",
                                "GT Auth Refresh Revocation Tenant",
                                "refresh-revocation@growtogether.africa",
                                "refresh-revocation",
                                PASSWORD,
                                "Refresh Revocation Administrator"
                        )
                );

        RequestContextHolder.set(
                new RequestContext(
                        "refresh-revocation-test",
                        tenant.tenantId().toString()
                )
        );

        LoginResponse login =
                authentication.login(
                        new LoginCommand(
                                "refresh-revocation",
                                PASSWORD
                        )
                );

        TokenResponse tokens =
                login.tokens();

        assertThat(tokens)
                .isNotNull();

        UserAccount user =
                users.findByIdAndTenantId(
                        tenant.administratorUserId(),
                        tenant.tenantId()
                ).orElseThrow();

        user.suspend();

        users.saveAndFlush(
                user
        );

        assertThatThrownBy(
                () -> authentication.refresh(
                        new RefreshCommand(
                                tokens.refreshToken()
                        )
                )
        )
                .isInstanceOf(
                        AuthenticationException.class
                )
                .hasMessage(
                        "Account is not available for authentication."
                );

        SessionState state =
                jdbc.queryForObject(
                        """
                        SELECT
                            revoked_at,
                            revoke_reason
                        FROM eiam_user_session
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        (rs, rowNum) ->
                                new SessionState(
                                        toInstant(
                                                rs.getTimestamp(
                                                        "revoked_at"
                                                )
                                        ),
                                        rs.getString(
                                                "revoke_reason"
                                        )
                                ),
                        tenant.tenantId(),
                        tokens.sessionId()
                );

        assertThat(state)
                .isNotNull();

        assertThat(state.revokedAt())
                .as(
                        "security revocation must survive rejected refresh"
                )
                .isNotNull();

        assertThat(state.revokeReason())
                .isEqualTo(
                        "ACCOUNT_UNAVAILABLE"
                );
    }

    @Test
    void mfaRequiredRefreshRevocationMustSurviveRejectedRefresh() {
        TenantView tenant =
                tenants.provision(
                        new ProvisionTenantCommand(
                                "GT-AUTH-REFRESH-MFA-ORG",
                                "GT Auth Refresh MFA Organisation",
                                "GT-AUTH-REFRESH-MFA",
                                "GT Auth Refresh MFA Tenant",
                                "refresh-mfa@growtogether.africa",
                                "refresh-mfa",
                                PASSWORD,
                                "Refresh MFA Administrator"
                        )
                );

        RequestContextHolder.set(
                new RequestContext(
                        "refresh-mfa-test",
                        tenant.tenantId().toString()
                )
        );

        /*
         * Issue a normal AAL1 session before MFA is enabled.
         */
        LoginResponse login =
                authentication.login(
                        new LoginCommand(
                                "refresh-mfa",
                                PASSWORD
                        )
                );

        TokenResponse tokens =
                login.tokens();

        assertThat(tokens)
                .isNotNull();

        /*
         * Test fixture: enable MFA after the AAL1 session exists.
         *
         * This isolated Testcontainers row represents the authoritative
         * enabled-MFA state. The refresh test does not exercise enrollment
         * or TOTP verification; it exercises the security consequence for
         * an older session that has not been MFA verified.
         */
        jdbc.update(
                """
                INSERT INTO eiam_mfa_profile (
                    id,
                    tenant_id,
                    user_id,
                    encrypted_totp_secret,
                    enabled,
                    enrolled_at,
                    last_verified_at,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    TRUE,
                    CURRENT_TIMESTAMP,
                    CURRENT_TIMESTAMP,
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0,
                    'ACTIVE'
                )
                """,
                java.util.UUID.randomUUID(),
                tenant.tenantId(),
                tenant.administratorUserId(),
                "TEST-MFA-SECRET-NOT-FOR-AUTHENTICATOR",
                "authentication-refresh-test",
                "authentication-refresh-test"
        );

        assertThatThrownBy(
                () -> authentication.refresh(
                        new RefreshCommand(
                                tokens.refreshToken()
                        )
                )
        )
                .isInstanceOf(
                        AuthenticationException.class
                )
                .hasMessage(
                        "MFA verification is required."
                );

        SessionState state =
                jdbc.queryForObject(
                        """
                        SELECT
                            revoked_at,
                            revoke_reason
                        FROM eiam_user_session
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        (rs, rowNum) ->
                                new SessionState(
                                        toInstant(
                                                rs.getTimestamp(
                                                        "revoked_at"
                                                )
                                        ),
                                        rs.getString(
                                                "revoke_reason"
                                        )
                                ),
                        tenant.tenantId(),
                        tokens.sessionId()
                );

        assertThat(state)
                .isNotNull();

        assertThat(state.revokedAt())
                .as(
                        "MFA_REQUIRED revocation must survive rejected refresh"
                )
                .isNotNull();

        assertThat(state.revokeReason())
                .isEqualTo(
                        "MFA_REQUIRED"
                );
    }


    private static Instant toInstant(
            Timestamp timestamp
    ) {
        return timestamp == null
                ? null
                : timestamp.toInstant();
    }

    private record SessionState(
            Instant revokedAt,
            String revokeReason
    ) {}
}
