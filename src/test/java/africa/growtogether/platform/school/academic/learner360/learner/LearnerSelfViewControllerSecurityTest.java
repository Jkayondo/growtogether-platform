package africa.growtogether.platform.school.academic.learner360.learner;

import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.error.GlobalExceptionHandler;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.common.security.GtPrincipal;
import africa.growtogether.platform.common.security.JwtAuthenticationFilter;
import africa.growtogether.platform.common.security.JwtService;
import africa.growtogether.platform.common.security.SecurityConfiguration;
import africa.growtogether.platform.common.security.SecurityErrorWriter;
import africa.growtogether.platform.common.security.TenantBoundaryFilter;
import africa.growtogether.platform.common.web.RequestContextFilter;
import africa.growtogether.platform.school.learner.security.AuthenticatedLearnerResolver;
import africa.growtogether.platform.school.learner.security.AuthenticatedLearnerResolver.ResolvedLearner;
import africa.growtogether.platform.school.student.Student;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * L05D_AUTHENTICATED_LEARNER_HTTP_SECURITY
 *
 * Verifies the real GT HTTP security boundary for learner self-service.
 */
@WebMvcTest(LearnerSelfViewController.class)
@EnableWebSecurity
@Import({
        ApiResponses.class,
        RequestContextFilter.class,
        GlobalExceptionHandler.class,
        SecurityErrorWriter.class,
        SecurityConfiguration.class,
        JwtAuthenticationFilter.class,
        TenantBoundaryFilter.class,
        EnterpriseIdentityContext.class,
        JwtService.class
})
@TestPropertySource(
        properties = {
                "gt.security.jwt.issuer=gt-test",
                "gt.security.jwt.secret=01234567890123456789012345678901",
                "gt.security.jwt.access-token-seconds=300"
        }
)
class LearnerSelfViewControllerSecurityTest {

    private static final String ENDPOINT =
            "/api/school/learner/intelligence/me";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private LearnerSelfViewService service;

    @MockitoBean
    private AuthenticatedLearnerResolver learners;

    @Test
    void unauthenticatedLearnerSelfRequestIsRejected()
            throws Exception {

        mockMvc.perform(
                get(ENDPOINT)
        )
                .andExpect(
                        status().isUnauthorized()
                );

        verifyNoInteractions(
                learners,
                service
        );
    }

    @Test
    void crossTenantHeaderIsRejectedBeforeLearnerResolution()
            throws Exception {

        UUID userId =
                UUID.randomUUID();

        UUID authenticatedTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        mockMvc.perform(
                get(ENDPOINT)
                        .header(
                                "Authorization",
                                "Bearer "
                                        + token(
                                                userId,
                                                authenticatedTenant
                                        )
                        )
                        .header(
                                "X-Tenant-ID",
                                requestedTenant.toString()
                        )
        )
                .andExpect(
                        status().isForbidden()
                );

        verifyNoInteractions(
                learners,
                service
        );
    }

    @Test
    void unresolvedAuthenticatedUserCannotReadLearnerSelfView()
            throws Exception {

        UUID userId =
                UUID.randomUUID();

        UUID tenantId =
                UUID.randomUUID();

        when(
                learners.requireCurrentLearner()
        ).thenThrow(
                new AccessDeniedException(
                        "Learner mapping unavailable"
                )
        );

        mockMvc.perform(
                get(ENDPOINT)
                        .header(
                                "Authorization",
                                "Bearer "
                                        + token(
                                                userId,
                                                tenantId
                                        )
                        )
                        .header(
                                "X-Tenant-ID",
                                tenantId.toString()
                        )
        )
                .andExpect(
                        status().isForbidden()
                );

        verifyNoInteractions(service);
    }

    @Test
    void authenticatedResolvedLearnerCanReadOwnSelfView()
            throws Exception {

        UUID userId =
                UUID.randomUUID();

        UUID tenantId =
                UUID.randomUUID();

        UUID learnerId =
                UUID.randomUUID();

        Student student =
                org.mockito.Mockito.mock(
                        Student.class
                );

        when(student.getId())
                .thenReturn(learnerId);

        when(
                learners.requireCurrentLearner()
        ).thenReturn(
                new ResolvedLearner(
                        tenantId,
                        userId,
                        student
                )
        );

        /*
         * HTTP security is the subject of this slice test.
         * Returning null avoids introducing serialization assumptions
         * about LearnerSelfView while still verifying controller flow.
         */
        when(
                service.getLearnerView(
                        tenantId,
                        learnerId
                )
        ).thenReturn(null);

        mockMvc.perform(
                get(ENDPOINT)
                        .header(
                                "Authorization",
                                "Bearer "
                                        + token(
                                                userId,
                                                tenantId
                                        )
                        )
                        .header(
                                "X-Tenant-ID",
                                tenantId.toString()
                        )
        )
                .andExpect(
                        status().isOk()
                );

        verify(learners)
                .requireCurrentLearner();

        verify(service)
                .getLearnerView(
                        tenantId,
                        learnerId
                );
    }

    @Test
    void callerSuppliedLearnerAndTenantParametersCannotSubstituteIdentity()
            throws Exception {

        UUID userId =
                UUID.randomUUID();

        UUID tenantId =
                UUID.randomUUID();

        UUID authoritativeLearnerId =
                UUID.randomUUID();

        UUID attemptedLearnerId =
                UUID.randomUUID();

        UUID attemptedTenantId =
                UUID.randomUUID();

        Student student =
                org.mockito.Mockito.mock(
                        Student.class
                );

        when(student.getId())
                .thenReturn(
                        authoritativeLearnerId
                );

        when(
                learners.requireCurrentLearner()
        ).thenReturn(
                new ResolvedLearner(
                        tenantId,
                        userId,
                        student
                )
        );

        when(
                service.getLearnerView(
                        tenantId,
                        authoritativeLearnerId
                )
        ).thenReturn(null);

        mockMvc.perform(
                get(ENDPOINT)
                        .header(
                                "Authorization",
                                "Bearer "
                                        + token(
                                                userId,
                                                tenantId
                                        )
                        )
                        .header(
                                "X-Tenant-ID",
                                tenantId.toString()
                        )
                        .param(
                                "learnerId",
                                attemptedLearnerId.toString()
                        )
                        .param(
                                "tenantId",
                                attemptedTenantId.toString()
                        )
        )
                .andExpect(
                        status().isOk()
                );

        verify(learners)
                .requireCurrentLearner();

        verify(service)
                .getLearnerView(
                        tenantId,
                        authoritativeLearnerId
                );
    }

    @Test
    void callerSelectedLearnerPathIsNotExposed()
            throws Exception {

        UUID userId =
                UUID.randomUUID();

        UUID tenantId =
                UUID.randomUUID();

        UUID attemptedLearnerId =
                UUID.randomUUID();

        mockMvc.perform(
                get(
                        "/api/school/learner/intelligence/"
                                + attemptedLearnerId
                )
                        .header(
                                "Authorization",
                                "Bearer "
                                        + token(
                                                userId,
                                                tenantId
                                        )
                        )
                        .header(
                                "X-Tenant-ID",
                                tenantId.toString()
                        )
        )
                .andExpect(
                        handler().handlerType(ResourceHttpRequestHandler.class)
                );

        verifyNoInteractions(
                learners,
                service
        );
    }

    private String token(
            UUID userId,
            UUID tenantId
    ) {

        GtPrincipal principal =
                new GtPrincipal(
                        userId,
                        "learner-self-security-test-user",
                        tenantId,
                        Set.of("LEARNER"),
                        Set.of(),
                        UUID.randomUUID()
                );

        return jwtService.issueAccessToken(
                principal
        );
    }
}
