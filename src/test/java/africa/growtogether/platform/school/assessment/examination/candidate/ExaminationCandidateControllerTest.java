package africa.growtogether.platform.school.assessment.examination.candidate;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ExaminationCandidateControllerTest {


    @Mock
    private ExaminationCandidateService service;

    @Mock
    private EnterpriseIdentityContext identity;

    @Mock
    private ExaminationCandidate candidate;


    private ExaminationCandidateController controller;


    @BeforeEach
    void setUp() {

        controller =
                new ExaminationCandidateController(
                        service,
                        identity
                );
    }


    @Test
    void registerUsesAuthenticatedTenant() {

        UUID tenantId =
                UUID.randomUUID();

        UUID examinationSessionId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID studentEnrollmentId =
                UUID.randomUUID();

        CreateExaminationCandidateCommand command =
                new CreateExaminationCandidateCommand(
                        "CAND-API-001",
                        examinationSessionId,
                        studentId,
                        studentEnrollmentId
                );

        when(
                identity.requireTenantId()
        ).thenReturn(
                tenantId
        );

        when(
                service.register(
                        tenantId,
                        "CAND-API-001",
                        examinationSessionId,
                        studentId,
                        studentEnrollmentId
                )
        ).thenReturn(
                candidate
        );

        assertThat(
                controller.register(command)
        ).isSameAs(
                candidate
        );

        verify(service).register(
                tenantId,
                "CAND-API-001",
                examinationSessionId,
                studentId,
                studentEnrollmentId
        );
    }


    @Test
    void getUsesAuthenticatedTenant() {

        UUID tenantId =
                UUID.randomUUID();

        when(
                identity.requireTenantId()
        ).thenReturn(
                tenantId
        );

        when(
                service.get(
                        tenantId,
                        "CAND-API-002"
                )
        ).thenReturn(
                candidate
        );

        assertThat(
                controller.get(
                        "CAND-API-002"
                )
        ).isSameAs(
                candidate
        );

        verify(service).get(
                tenantId,
                "CAND-API-002"
        );
    }


    @Test
    void candidateEndpointsCarryAssessmentAuthorities()
            throws Exception {

        Method register =
                ExaminationCandidateController.class
                        .getMethod(
                                "register",
                                CreateExaminationCandidateCommand.class
                        );

        Method get =
                ExaminationCandidateController.class
                        .getMethod(
                                "get",
                                String.class
                        );

        PreAuthorize createAuthority =
                register.getAnnotation(
                        PreAuthorize.class
                );

        PreAuthorize readAuthority =
                get.getAnnotation(
                        PreAuthorize.class
                );

        assertThat(
                createAuthority
        ).isNotNull();

        assertThat(
                createAuthority.value()
        ).isEqualTo(
                "hasAuthority('school.academic.assessment.create')"
        );

        assertThat(
                readAuthority
        ).isNotNull();

        assertThat(
                readAuthority.value()
        ).isEqualTo(
                "hasAuthority('school.academic.assessment.read')"
        );
    }


    @Test
    void candidateControllerRetainsLegacyRouteAndAddsVersionedRoute() {

        RequestMapping mapping =
                ExaminationCandidateController.class
                        .getAnnotation(
                                RequestMapping.class
                        );

        assertThat(
                mapping
        ).isNotNull();

        assertThat(
                mapping.value()
        ).contains(
                "/api/v1/school/examination-candidates",
                "/api/school/examination-candidates"
        );
    }

}
