package africa.growtogether.platform.school.assessment.examination;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ExaminationSessionControllerTest {

    @Mock
    ExaminationSessionService service;

    @Mock
    EnterpriseIdentityContext identity;

    ExaminationSessionController controller;

    UUID tenantId;
    UUID userId;


    @BeforeEach
    void setUp() {

        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();

        controller =
                new ExaminationSessionController(
                        service,
                        identity
                );
    }


    @Test
    void createUsesAuthenticatedTenant() {

        CreateExaminationSessionCommand command =
                command();

        when(
                identity.requireTenantId()
        ).thenReturn(tenantId);


        controller.create(command);


        verify(service).create(
                tenantId,
                command
        );
    }


    @Test
    void readOperationsUseAuthenticatedTenant() {

        UUID id =
                UUID.randomUUID();

        when(
                identity.requireTenantId()
        ).thenReturn(tenantId);


        controller.get(id);
        controller.getByCode("EX-2026-001");


        verify(service).get(
                tenantId,
                id
        );

        verify(service).getByCode(
                tenantId,
                "EX-2026-001"
        );
    }


    @Test
    void approveUsesAuthenticatedTenantAndUser() {

        UUID id =
                UUID.randomUUID();

        when(
                identity.requireTenantId()
        ).thenReturn(tenantId);

        when(
                identity.requireUserId()
        ).thenReturn(userId);


        controller.approve(id);


        verify(service).approve(
                tenantId,
                id,
                userId
        );
    }


    @Test
    void lifecycleOperationsUseAuthenticatedTenant() {

        UUID id =
                UUID.randomUUID();

        when(
                identity.requireTenantId()
        ).thenReturn(tenantId);


        controller.openRegistration(id);
        controller.activate(id);
        controller.complete(id);


        verify(service).openRegistration(
                tenantId,
                id
        );

        verify(service).activate(
                tenantId,
                id
        );

        verify(service).complete(
                tenantId,
                id
        );
    }


    @Test
    void preservesModernAndLegacyRoutes() {

        RequestMapping mapping =
                ExaminationSessionController.class
                        .getAnnotation(
                                RequestMapping.class
                        );

        assertThat(mapping)
                .isNotNull();

        assertThat(mapping.value())
                .containsExactlyInAnyOrder(
                        "/api/v1/school/examination-sessions",
                        "/school/examination-sessions"
                );
    }


    @Test
    void usesAssessmentPermissions()
            throws Exception {

        permission(
                "create",
                new Class<?>[]{
                        CreateExaminationSessionCommand.class
                },
                "hasAuthority('school.academic.assessment.create')"
        );

        permission(
                "get",
                new Class<?>[]{
                        UUID.class
                },
                "hasAuthority('school.academic.assessment.read')"
        );

        permission(
                "getByCode",
                new Class<?>[]{
                        String.class
                },
                "hasAuthority('school.academic.assessment.read')"
        );

        permission(
                "approve",
                new Class<?>[]{
                        UUID.class
                },
                "hasAuthority('school.academic.assessment.manage')"
        );

        permission(
                "openRegistration",
                new Class<?>[]{
                        UUID.class
                },
                "hasAuthority('school.academic.assessment.manage')"
        );

        permission(
                "activate",
                new Class<?>[]{
                        UUID.class
                },
                "hasAuthority('school.academic.assessment.manage')"
        );

        permission(
                "complete",
                new Class<?>[]{
                        UUID.class
                },
                "hasAuthority('school.academic.assessment.manage')"
        );
    }


    private void permission(
            String methodName,
            Class<?>[] parameterTypes,
            String expected
    ) throws Exception {

        Method method =
                ExaminationSessionController.class
                        .getMethod(
                                methodName,
                                parameterTypes
                        );

        PreAuthorize annotation =
                method.getAnnotation(
                        PreAuthorize.class
                );

        assertThat(annotation)
                .isNotNull();

        assertThat(annotation.value())
                .isEqualTo(expected);
    }


    private CreateExaminationSessionCommand command() {

        return new CreateExaminationSessionCommand(
                "EX-2026-001",
                "Term One Examination",
                null,
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                "INTERNAL",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                null,
                null,
                null,
                null,
                null
        );
    }
}
