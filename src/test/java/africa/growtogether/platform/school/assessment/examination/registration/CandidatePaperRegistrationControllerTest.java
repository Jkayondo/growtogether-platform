package africa.growtogether.platform.school.assessment.examination.registration;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


class CandidatePaperRegistrationControllerTest {


    private CandidatePaperRegistrationService service;
    private EnterpriseIdentityContext identity;

    private CandidatePaperRegistrationController controller;

    private UUID tenantId;
    private UUID actorId;


    @BeforeEach
    void setUp() {

        service =
                mock(
                        CandidatePaperRegistrationService.class
                );

        identity =
                mock(
                        EnterpriseIdentityContext.class
                );

        controller =
                new CandidatePaperRegistrationController(
                        service,
                        identity
                );

        tenantId =
                UUID.randomUUID();

        actorId =
                UUID.randomUUID();


        when(
                identity.requireTenantId()
        ).thenReturn(
                tenantId
        );

        when(
                identity.requireUserId()
        ).thenReturn(
                actorId
        );
    }


    @Test
    void registerUsesIdentityTenantAndActor() {

        CreateCandidatePaperRegistrationCommand command =
                new CreateCandidatePaperRegistrationCommand(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        "STANDARD"
                );


        controller.register(
                command
        );


        verify(
                service
        ).register(
                tenantId,
                actorId,
                command
        );
    }


    @Test
    void getUsesIdentityTenant() {

        UUID candidateId =
                UUID.randomUUID();

        UUID paperId =
                UUID.randomUUID();


        controller.get(
                candidateId,
                paperId
        );


        verify(
                service
        ).get(
                tenantId,
                candidateId,
                paperId
        );
    }


    @Test
    void verifyUsesIdentityTenant() {

        UUID candidateId =
                UUID.randomUUID();

        UUID paperId =
                UUID.randomUUID();


        controller.verify(
                candidateId,
                paperId
        );


        verify(
                service
        ).verify(
                tenantId,
                candidateId,
                paperId
        );
    }


    @Test
    void exposesModernAndLegacyRoutes() {

        RequestMapping mapping =
                CandidatePaperRegistrationController.class
                        .getAnnotation(
                                RequestMapping.class
                        );


        assertThat(
                mapping
        ).isNotNull();

        assertThat(
                mapping.value()
        ).containsExactlyInAnyOrder(
                "/api/v1/school/candidate-paper-registrations",
                "/api/school/candidate-paper-registrations"
        );
    }


    @Test
    void endpointsUseAssessmentPermissions()
            throws Exception {

        Method register =
                CandidatePaperRegistrationController.class
                        .getDeclaredMethod(
                                "register",
                                CreateCandidatePaperRegistrationCommand.class
                        );

        Method get =
                CandidatePaperRegistrationController.class
                        .getDeclaredMethod(
                                "get",
                                UUID.class,
                                UUID.class
                        );

        Method verify =
                CandidatePaperRegistrationController.class
                        .getDeclaredMethod(
                                "verify",
                                UUID.class,
                                UUID.class
                        );


        assertThat(
                register.getAnnotation(
                        PostMapping.class
                )
        ).isNotNull();

        assertThat(
                get.getAnnotation(
                        GetMapping.class
                )
        ).isNotNull();

        PostMapping verifyMapping =
                verify.getAnnotation(
                        PostMapping.class
                );

        assertThat(
                verifyMapping
        ).isNotNull();

        assertThat(
                verifyMapping.value()
        ).containsExactly(
                "/verify"
        );


        assertThat(
                register
                        .getAnnotation(
                                PreAuthorize.class
                        )
                        .value()
        ).isEqualTo(
                "hasAuthority('school.academic.assessment.create')"
        );

        assertThat(
                get
                        .getAnnotation(
                                PreAuthorize.class
                        )
                        .value()
        ).isEqualTo(
                "hasAuthority('school.academic.assessment.read')"
        );

        assertThat(
                verify
                        .getAnnotation(
                                PreAuthorize.class
                        )
                        .value()
        ).isEqualTo(
                "hasAuthority('school.academic.assessment.manage')"
        );
    }

}
