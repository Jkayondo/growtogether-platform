package africa.growtogether.platform.eiam.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.common.security.GtPrincipal;
import africa.growtogether.platform.common.security.JwtProperties;
import africa.growtogether.platform.common.security.JwtService;
import africa.growtogether.platform.common.security.PasswordService;
import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.eiam.mfa.MfaService;
import africa.growtogether.platform.eiam.permission.EffectiveAuthorityService;
import africa.growtogether.platform.eiam.user.UserAccount;
import africa.growtogether.platform.eiam.user.UserAccountRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticationPhoneIdentityTest {

    @Mock
    private UserAccountRepository users;

    @Mock
    private UserSessionRepository sessions;

    @Mock
    private PasswordService passwords;

    @Mock
    private EffectiveAuthorityService authorities;

    @Mock
    private JwtService jwt;

    @Mock
    private RefreshTokenService refreshTokens;

    @Mock
    private MfaService mfa;

    @Mock
    private AuthenticationSecurityStateService securityState;

    @Mock
    private UserAccount user;

    private UUID tenantId;
    private UUID userId;
    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();

        RequestContextHolder.set(
                new RequestContext(
                        "authentication-phone-test",
                        tenantId.toString()
                )
        );

        service =
                new AuthenticationService(
                        users,
                        sessions,
                        passwords,
                        authorities,
                        jwt,
                        new JwtProperties(
                                "growtogether-test",
                                "test-secret",
                                900
                        ),
                        new AuthProperties(
                                86400,
                                5,
                                900
                        ),
                        refreshTokens,
                        mfa,
                        securityState
                );
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Test
    void verifiedPhoneCanAuthenticateThroughExistingSecurityFlow() {
        String phone =
                "+256701234567";

        when(
                users.findByTenantIdAndUsernameIgnoreCase(
                        tenantId,
                        phone
                )
        ).thenReturn(Optional.empty());

        when(
                users.findByTenantIdAndEmailIgnoreCase(
                        tenantId,
                        phone
                )
        ).thenReturn(Optional.empty());

        when(
                users
                        .findByTenantIdAndPrimaryPhoneNumberAndPhoneVerifiedAtIsNotNull(
                                tenantId,
                                phone
                        )
        ).thenReturn(Optional.of(user));

        when(
                user.canAuthenticateAt(
                        any(Instant.class)
                )
        ).thenReturn(true);

        when(user.getPasswordHash())
                .thenReturn("encoded-password");

        when(user.getId())
                .thenReturn(userId);

        when(user.getTenantId())
                .thenReturn(tenantId);

        when(user.getUsername())
                .thenReturn("parent.guardian");

        when(
                passwords.matches(
                        "correct-password",
                        "encoded-password"
                )
        ).thenReturn(true);

        when(
                mfa.enabled(
                        tenantId,
                        userId
                )
        ).thenReturn(false);

        when(
                authorities.resolve(
                        tenantId,
                        userId
                )
        ).thenReturn(
                new EffectiveAuthorityService.EffectiveAuthorities(
                        Set.of("PARENT"),
                        Set.of("school.portal.access")
                )
        );

        when(refreshTokens.generate())
                .thenReturn("refresh-token");

        when(
                refreshTokens.hash(
                        "refresh-token"
                )
        ).thenReturn(
                "a".repeat(64)
        );

        when(
                jwt.issueAccessToken(
                        any(GtPrincipal.class),
                        anyString()
                )
        ).thenReturn(
                "access-token"
        );

        LoginResponse result =
                service.login(
                        new LoginCommand(
                                phone,
                                "correct-password"
                        )
                );

        assertNotNull(result);
        assertFalse(result.mfaRequired());
        assertNotNull(result.tokens());

        assertEquals(
                "access-token",
                result.tokens().accessToken()
        );

        assertEquals(
                userId,
                result.tokens().userId()
        );

        assertEquals(
                tenantId,
                result.tokens().tenantId()
        );

        assertEquals(
                "parent.guardian",
                result.tokens().username()
        );

        assertEquals(
                Set.of("PARENT"),
                result.tokens().roles()
        );

        verify(users)
                .findByTenantIdAndPrimaryPhoneNumberAndPhoneVerifiedAtIsNotNull(
                        tenantId,
                        phone
                );

        verify(passwords)
                .matches(
                        "correct-password",
                        "encoded-password"
                );

        verify(sessions)
                .save(
                        any(UserSession.class)
                );
    }

    @Test
    void phoneNotReturnedByVerifiedLookupCannotAuthenticate() {
        String phone =
                "+256701234567";

        when(
                users.findByTenantIdAndUsernameIgnoreCase(
                        tenantId,
                        phone
                )
        ).thenReturn(Optional.empty());

        when(
                users.findByTenantIdAndEmailIgnoreCase(
                        tenantId,
                        phone
                )
        ).thenReturn(Optional.empty());

        when(
                users
                        .findByTenantIdAndPrimaryPhoneNumberAndPhoneVerifiedAtIsNotNull(
                                tenantId,
                                phone
                        )
        ).thenReturn(Optional.empty());

        AuthenticationException exception =
                assertThrows(
                        AuthenticationException.class,
                        () -> service.login(
                                new LoginCommand(
                                        phone,
                                        "correct-password"
                                )
                        )
                );

        assertEquals(
                "Invalid username or password.",
                exception.getMessage()
        );

        verify(users)
                .findByTenantIdAndPrimaryPhoneNumberAndPhoneVerifiedAtIsNotNull(
                        tenantId,
                        phone
                );

        verify(
                passwords,
                never()
        ).matches(
                anyString(),
                anyString()
        );
    }
}
