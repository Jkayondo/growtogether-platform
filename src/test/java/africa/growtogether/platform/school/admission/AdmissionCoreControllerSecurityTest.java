package africa.growtogether.platform.school.admission;

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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest({
        AdmissionApplicationController.class,
        AdmissionGuardianController.class
})
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
class AdmissionCoreControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private AdmissionApplicationService applicationService;

    @MockitoBean
    private AdmissionGuardianService guardianService;


    @Test
    void authorisedAdministratorCreatesApplicationUsingAuthenticatedTenant()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID applicationId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();

        AdmissionApplication application =
                mock(
                        AdmissionApplication.class
                );

        when(application.getId())
                .thenReturn(applicationId);

        when(application.getApplicationNumber())
                .thenReturn("B8-DEV-ADM-000001");

        when(application.getAcademicYearId())
                .thenReturn(academicYearId);

        when(application.getCampusId())
                .thenReturn(campusId);

        when(application.getDesiredClassGradeId())
                .thenReturn(classGradeId);

        when(application.getApplicationDate())
                .thenReturn(LocalDate.of(2026, 8, 26));

        when(application.getAdmissionStatus())
                .thenReturn("DRAFT");

        when(application.getSubmissionChannel())
                .thenReturn("OFFICE");

        when(
                applicationService.createDraft(
                        eq(tenantId),
                        any(CreateAdmissionApplicationCommand.class)
                )
        ).thenReturn(
                application
        );

        String token =
                token(
                        tenantId,
                        Set.of(
                                "school.admission.application.manage"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/admissions"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .param(
                                        "tenantId",
                                        UUID.randomUUID().toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "academicYearId": "%s",
                                          "campusId": "%s",
                                          "desiredClassGradeId": "%s",
                                          "submissionChannel": "OFFICE"
                                        }
                                        """.formatted(
                                                academicYearId,
                                                campusId,
                                                classGradeId
                                        )
                                )
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-SCHOOL-ADMISSION-CORE-001"
                                )
                )
                .andExpect(
                        jsonPath("$.data.id")
                                .value(
                                        applicationId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.admissionStatus")
                                .value("DRAFT")
                );

        verify(applicationService)
                .createDraft(
                        eq(tenantId),
                        any(CreateAdmissionApplicationCommand.class)
                );
    }


    @Test
    void applicationCreationWithoutApplicationAuthorityIsForbidden()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        String token =
                token(
                        tenantId,
                        Set.of(
                                "school.admission.guardian.manage"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/admissions"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "academicYearId": "%s",
                                          "campusId": "%s",
                                          "desiredClassGradeId": "%s"
                                        }
                                        """.formatted(
                                                UUID.randomUUID(),
                                                UUID.randomUUID(),
                                                UUID.randomUUID()
                                        )
                                )
                )
                .andExpect(
                        status().isForbidden()
                );

        verify(
                applicationService,
                never()
        ).createDraft(
                any(),
                any()
        );
    }


    @Test
    void authorisedAdministratorCreatesGuardianUsingAuthenticatedTenant()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID applicationId =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        AdmissionGuardian guardian =
                mock(
                        AdmissionGuardian.class
                );

        when(guardian.getId())
                .thenReturn(guardianId);

        when(guardian.getAdmissionApplicationId())
                .thenReturn(applicationId);

        when(guardian.getRelationshipType())
                .thenReturn("FATHER");

        when(guardian.getFirstName())
                .thenReturn("B8");

        when(guardian.getLastName())
                .thenReturn("Guardian");

        when(guardian.getPhoneNumber())
                .thenReturn("0700000000");

        when(guardian.getEmail())
                .thenReturn("johnkkayondo@gmail.com");

        when(guardian.isPrimaryGuardian())
                .thenReturn(true);

        when(guardian.isReceivesCommunications())
                .thenReturn(true);

        when(
                guardianService.create(
                        eq(tenantId),
                        eq(applicationId),
                        any(CreateAdmissionGuardianCommand.class)
                )
        ).thenReturn(
                guardian
        );

        String token =
                token(
                        tenantId,
                        Set.of(
                                "school.admission.guardian.manage"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/admissions/"
                                        + applicationId
                                        + "/guardians"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "relationshipType": "FATHER",
                                          "firstName": "B8",
                                          "lastName": "Guardian",
                                          "phoneNumber": "0700000000",
                                          "email": "johnkkayondo@gmail.com",
                                          "primaryGuardian": true,
                                          "receivesCommunications": true
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-SCHOOL-ADMISSION-GUARDIAN-001"
                                )
                )
                .andExpect(
                        jsonPath("$.data.id")
                                .value(
                                        guardianId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.email")
                                .value(
                                        "johnkkayondo@gmail.com"
                                )
                );

        verify(guardianService)
                .create(
                        eq(tenantId),
                        eq(applicationId),
                        any(CreateAdmissionGuardianCommand.class)
                );
    }


    @Test
    void guardianCreationWithoutGuardianAuthorityIsForbidden()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID applicationId =
                UUID.randomUUID();

        String token =
                token(
                        tenantId,
                        Set.of(
                                "school.admission.application.manage"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/admissions/"
                                        + applicationId
                                        + "/guardians"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "relationshipType": "FATHER",
                                          "firstName": "B8",
                                          "lastName": "Guardian",
                                          "phoneNumber": "0700000000",
                                          "email": "johnkkayondo@gmail.com"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isForbidden()
                );

        verify(
                guardianService,
                never()
        ).create(
                any(),
                any(),
                any()
        );
    }


    @Test
    void crossTenantAdmissionCreationIsRejectedBeforeService()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        String token =
                token(
                        principalTenant,
                        Set.of(
                                "school.admission.application.manage"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/admissions"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        requestedTenant.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "academicYearId": "%s",
                                          "campusId": "%s",
                                          "desiredClassGradeId": "%s"
                                        }
                                        """.formatted(
                                                UUID.randomUUID(),
                                                UUID.randomUUID(),
                                                UUID.randomUUID()
                                        )
                                )
                )
                .andExpect(
                        status().isForbidden()
                );

        verify(
                applicationService,
                never()
        ).createDraft(
                any(),
                any()
        );
    }


    @Test
    void unauthenticatedGuardianCreationIsRejected()
            throws Exception {

        UUID applicationId =
                UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/v1/school/admissions/"
                                        + applicationId
                                        + "/guardians"
                        )
                                .header(
                                        "X-Tenant-ID",
                                        UUID.randomUUID().toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "relationshipType": "FATHER",
                                          "firstName": "B8",
                                          "lastName": "Guardian",
                                          "phoneNumber": "0700000000",
                                          "email": "johnkkayondo@gmail.com"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );

        verify(
                guardianService,
                never()
        ).create(
                any(),
                any(),
                any()
        );
    }


    private String token(
            UUID tenantId,
            Set<String> permissions
    ) {

        GtPrincipal principal =
                new GtPrincipal(
                        UUID.randomUUID(),
                        "admission-core-http-user",
                        tenantId,
                        Set.of(
                                "SCHOOL_ADMIN"
                        ),
                        permissions,
                        UUID.randomUUID()
                );

        return jwtService.issueAccessToken(
                principal
        );
    }
}
