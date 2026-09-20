package africa.growtogether.platform.school.assessment.marking;


import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.junit.jupiter.api.Test;

import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class CandidateScoreControllerTest {


    @Test
    void createUsesIdentityTenant() {

        CandidateScoreService service =
                mock(
                        CandidateScoreService.class
                );

        EnterpriseIdentityContext identity =
                mock(
                        EnterpriseIdentityContext.class
                );

        CandidateScoreController controller =
                new CandidateScoreController(
                        service,
                        identity
                );


        UUID tenant =
                UUID.randomUUID();

        CreateCandidateScoreCommand command =
                new CreateCandidateScoreCommand(
                        UUID.randomUUID(),
                        null,
                        null,
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        CandidateScore expected =
                mock(
                        CandidateScore.class
                );


        when(
                identity.requireTenantId()
        )
                .thenReturn(
                        tenant
                );

        when(
                service.create(
                        tenant,
                        command
                )
        )
                .thenReturn(
                        expected
                );


        assertSame(
                expected,
                controller.create(
                        command
                )
        );
    }


    @Test
    void enterScoreUsesIdentityTenantAndActor() {

        CandidateScoreService service =
                mock(
                        CandidateScoreService.class
                );

        EnterpriseIdentityContext identity =
                mock(
                        EnterpriseIdentityContext.class
                );

        CandidateScoreController controller =
                new CandidateScoreController(
                        service,
                        identity
                );


        UUID tenant =
                UUID.randomUUID();

        UUID actor =
                UUID.randomUUID();

        UUID scoreId =
                UUID.randomUUID();

        BigDecimal value =
                new BigDecimal(
                        "77"
                );

        CandidateScore expected =
                mock(
                        CandidateScore.class
                );


        when(
                identity.requireTenantId()
        )
                .thenReturn(
                        tenant
                );

        when(
                identity.requireUserId()
        )
                .thenReturn(
                        actor
                );

        when(
                service.enterScore(
                        tenant,
                        scoreId,
                        value,
                        actor
                )
        )
                .thenReturn(
                        expected
                );


        assertSame(
                expected,
                controller.enterScore(
                        scoreId,
                        new EnterCandidateScoreCommand(
                                value
                        )
                )
        );
    }


    @Test
    void controllerPermissionsMatchAssessmentContract()
            throws Exception {

        assertEquals(
                "hasAuthority('school.academic.assessment.create')",
                CandidateScoreController.class
                        .getMethod(
                                "create",
                                CreateCandidateScoreCommand.class
                        )
                        .getAnnotation(
                                PreAuthorize.class
                        )
                        .value()
        );


        assertEquals(
                "hasAuthority('school.academic.assessment.read')",
                CandidateScoreController.class
                        .getMethod(
                                "get",
                                UUID.class
                        )
                        .getAnnotation(
                                PreAuthorize.class
                        )
                        .value()
        );


        assertEquals(
                "hasAuthority('school.academic.assessment.manage')",
                CandidateScoreController.class
                        .getMethod(
                                "enterScore",
                                UUID.class,
                                EnterCandidateScoreCommand.class
                        )
                        .getAnnotation(
                                PreAuthorize.class
                        )
                        .value()
        );


        assertEquals(
                "hasAuthority('school.academic.assessment.manage')",
                CandidateScoreController.class
                        .getMethod(
                                "markAbsent",
                                UUID.class
                        )
                        .getAnnotation(
                                PreAuthorize.class
                        )
                        .value()
        );
    }
}
