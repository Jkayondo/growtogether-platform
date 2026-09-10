package africa.growtogether.platform.school.assessment;

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

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AssessmentPlanController.class)
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
class AssessmentPlanControllerSecurityTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private AssessmentPlanService service;


    @Test
    void authenticatedCreatorUsesJwtTenantForAssessmentPlanCreate()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.create"
                        )
                );

        var commandCaptor =
                org.mockito.ArgumentCaptor.forClass(
                        CreateAssessmentPlanCommand.class
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/assessment-plans"
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
                                          "planCode": "AP-SEC-001",
                                          "planName": "Security Proof Plan",
                                          "description": "JWT tenant proof",
                                          "academicYearId": "%s",
                                          "academicTermId": null,
                                          "campusId": "%s",
                                          "academicProgrammeId": null,
                                          "studyTrackId": null,
                                          "curriculumVersionId": null,
                                          "classGradeId": "%s",
                                          "streamId": null,
                                          "gradingSchemeId": null,
                                          "effectiveFrom": "2026-02-02",
                                          "effectiveTo": "2026-04-30",
                                          "workflowInstanceId": null
                                        }
                                        """
                                                .formatted(
                                                        academicYearId,
                                                        campusId,
                                                        classGradeId
                                                )
                                )
                )
                .andExpect(
                        status().isOk()
                );

        verify(
                service
        ).create(
                eq(
                        tenantId
                ),
                commandCaptor.capture()
        );

        CreateAssessmentPlanCommand command =
                commandCaptor.getValue();

        assertEquals(
                "AP-SEC-001",
                command.planCode()
        );

        assertEquals(
                academicYearId,
                command.academicYearId()
        );

        assertEquals(
                campusId,
                command.campusId()
        );

        assertEquals(
                classGradeId,
                command.classGradeId()
        );
    }


    @Test
    void readPermissionCannotCreateAssessmentPlan()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.read"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/assessment-plans"
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
                                          "planCode": "AP-SEC-DENIED-001",
                                          "planName": "Denied Security Plan",
                                          "academicYearId": "%s",
                                          "academicTermId": null,
                                          "campusId": "%s",
                                          "academicProgrammeId": null,
                                          "studyTrackId": null,
                                          "curriculumVersionId": null,
                                          "classGradeId": "%s",
                                          "streamId": null,
                                          "gradingSchemeId": null,
                                          "effectiveFrom": "2026-02-02",
                                          "effectiveTo": "2026-04-30",
                                          "workflowInstanceId": null
                                        }
                                        """
                                                .formatted(
                                                        academicYearId,
                                                        campusId,
                                                        classGradeId
                                                )
                                )
                )
                .andExpect(
                        status().isForbidden()
                );

        org.mockito.Mockito.verifyNoInteractions(
                service
        );
    }


    @Test
    void rejectsCrossTenantAssessmentPlanCreateBeforeService()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        principalTenant,
                        Set.of(
                                "school.academic.assessment.create"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/assessment-plans"
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
                                          "planCode": "AP-CROSS-TENANT-001",
                                          "planName": "Cross Tenant Attempt",
                                          "academicYearId": "%s",
                                          "academicTermId": null,
                                          "campusId": "%s",
                                          "academicProgrammeId": null,
                                          "studyTrackId": null,
                                          "curriculumVersionId": null,
                                          "classGradeId": "%s",
                                          "streamId": null,
                                          "gradingSchemeId": null,
                                          "effectiveFrom": "2026-02-02",
                                          "effectiveTo": "2026-04-30",
                                          "workflowInstanceId": null
                                        }
                                        """
                                                .formatted(
                                                        academicYearId,
                                                        campusId,
                                                        classGradeId
                                                )
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.code")
                                .value("GT-TENANT-002")
                );

        org.mockito.Mockito.verifyNoInteractions(
                service
        );
    }


    @Test
    void rejectsAssessmentPlanCreateWithoutTenantHeader()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.create"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/school/assessment-plans"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "planCode": "AP-NO-TENANT-001",
                                          "planName": "Missing Tenant Header",
                                          "academicYearId": "%s",
                                          "academicTermId": null,
                                          "campusId": "%s",
                                          "academicProgrammeId": null,
                                          "studyTrackId": null,
                                          "curriculumVersionId": null,
                                          "classGradeId": "%s",
                                          "streamId": null,
                                          "gradingSchemeId": null,
                                          "effectiveFrom": "2026-02-02",
                                          "effectiveTo": "2026-04-30",
                                          "workflowInstanceId": null
                                        }
                                        """
                                                .formatted(
                                                        academicYearId,
                                                        campusId,
                                                        classGradeId
                                                )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.code")
                                .value("GT-TENANT-001")
                );

        org.mockito.Mockito.verifyNoInteractions(
                service
        );
    }


    @Test
    void rejectsUnauthenticatedAssessmentPlanCreate()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/v1/school/assessment-plans"
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
                                          "planCode": "AP-UNAUTH-001",
                                          "planName": "Unauthenticated Attempt",
                                          "academicYearId": "%s",
                                          "academicTermId": null,
                                          "campusId": "%s",
                                          "academicProgrammeId": null,
                                          "studyTrackId": null,
                                          "curriculumVersionId": null,
                                          "classGradeId": "%s",
                                          "streamId": null,
                                          "gradingSchemeId": null,
                                          "effectiveFrom": "2026-02-02",
                                          "effectiveTo": "2026-04-30",
                                          "workflowInstanceId": null
                                        }
                                        """
                                                .formatted(
                                                        academicYearId,
                                                        campusId,
                                                        classGradeId
                                                )
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.code")
                                .value("GT-AUTH-001")
                );

        org.mockito.Mockito.verifyNoInteractions(
                service
        );
    }


    @Test
    void authenticatedReaderUsesJwtTenantForAssessmentPlanGet()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID assessmentPlanId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.read"
                        )
                );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.get(
                                        "/api/v1/school/assessment-plans/"
                                                + assessmentPlanId
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                );

        verify(
                service
        ).get(
                tenantId,
                assessmentPlanId
        );
    }


    @Test
    void createPermissionCannotReadAssessmentPlan()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID assessmentPlanId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.create"
                        )
                );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.get(
                                        "/api/v1/school/assessment-plans/"
                                                + assessmentPlanId
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                );

        org.mockito.Mockito.verifyNoInteractions(
                service
        );
    }


    @Test
    void rejectsCrossTenantAssessmentPlanReadBeforeService()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID assessmentPlanId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        principalTenant,
                        Set.of(
                                "school.academic.assessment.read"
                        )
                );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.get(
                                        "/api/v1/school/assessment-plans/"
                                                + assessmentPlanId
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        requestedTenant.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.code")
                                .value("GT-TENANT-002")
                );

        org.mockito.Mockito.verifyNoInteractions(
                service
        );
    }


    @Test
    void rejectsAssessmentPlanReadWithoutTenantHeader()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID assessmentPlanId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.read"
                        )
                );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.get(
                                        "/api/v1/school/assessment-plans/"
                                                + assessmentPlanId
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.code")
                                .value("GT-TENANT-001")
                );

        org.mockito.Mockito.verifyNoInteractions(
                service
        );
    }


    @Test
    void rejectsUnauthenticatedAssessmentPlanRead()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID assessmentPlanId =
                UUID.randomUUID();

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.get(
                                        "/api/v1/school/assessment-plans/"
                                                + assessmentPlanId
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.code")
                                .value("GT-AUTH-001")
                );

        org.mockito.Mockito.verifyNoInteractions(
                service
        );
    }


    @Test
    void authenticatedReaderUsesJwtTenantForAssessmentPlanAcademicYearList()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.read"
                        )
                );

        org.mockito.Mockito.when(
                service.findByAcademicYear(
                        org.mockito.ArgumentMatchers.eq(
                                tenantId
                        ),
                        org.mockito.ArgumentMatchers.eq(
                                academicYearId
                        )
                )
        ).thenReturn(
                java.util.List.of()
        );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.get(
                                        "/api/v1/school/assessment-plans/academic-year/"
                                                + academicYearId
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                );

        org.mockito.Mockito.verify(
                service
        ).findByAcademicYear(
                tenantId,
                academicYearId
        );
    }


    @Test
    void createPermissionCannotReadAssessmentPlansByAcademicYear()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.create"
                        )
                );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.get(
                                        "/api/v1/school/assessment-plans/academic-year/"
                                                + academicYearId
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                );

        org.mockito.Mockito.verifyNoInteractions(
                service
        );
    }


    @Test
    void rejectsCrossTenantAssessmentPlanAcademicYearListBeforeService()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        principalTenant,
                        Set.of(
                                "school.academic.assessment.read"
                        )
                );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.get(
                                        "/api/v1/school/assessment-plans/academic-year/"
                                                + academicYearId
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        requestedTenant.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.code")
                                .value("GT-TENANT-002")
                );

        org.mockito.Mockito.verifyNoInteractions(
                service
        );
    }


    @Test
    void rejectsAssessmentPlanAcademicYearListWithoutTenantHeader()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.read"
                        )
                );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.get(
                                        "/api/v1/school/assessment-plans/academic-year/"
                                                + academicYearId
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.code")
                                .value("GT-TENANT-001")
                );

        org.mockito.Mockito.verifyNoInteractions(
                service
        );
    }


    @Test
    void rejectsUnauthenticatedAssessmentPlanAcademicYearList()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.get(
                                        "/api/v1/school/assessment-plans/academic-year/"
                                                + academicYearId
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.code")
                                .value("GT-AUTH-001")
                );

        org.mockito.Mockito.verifyNoInteractions(
                service
        );
    }


    @Test
    void authenticatedReaderUsesJwtTenantForAssessmentPlanCampusList()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.read"
                        )
                );

        org.mockito.Mockito.when(
                service.findByCampus(
                        org.mockito.ArgumentMatchers.eq(
                                tenantId
                        ),
                        org.mockito.ArgumentMatchers.eq(
                                campusId
                        )
                )
        ).thenReturn(
                java.util.List.of()
        );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.get(
                                        "/api/v1/school/assessment-plans/campus/"
                                                + campusId
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                );

        org.mockito.Mockito.verify(
                service
        ).findByCampus(
                tenantId,
                campusId
        );
    }


    @Test
    void createPermissionCannotReadAssessmentPlansByCampus()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.create"
                        )
                );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.get(
                                        "/api/v1/school/assessment-plans/campus/"
                                                + campusId
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                );

        org.mockito.Mockito.verifyNoInteractions(
                service
        );
    }


    @Test
    void rejectsCrossTenantAssessmentPlanCampusListBeforeService()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        principalTenant,
                        Set.of(
                                "school.academic.assessment.read"
                        )
                );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.get(
                                        "/api/v1/school/assessment-plans/campus/"
                                                + campusId
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        requestedTenant.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.code")
                                .value("GT-TENANT-002")
                );

        org.mockito.Mockito.verifyNoInteractions(
                service
        );
    }


    @Test
    void rejectsAssessmentPlanCampusListWithoutTenantHeader()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.read"
                        )
                );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.get(
                                        "/api/v1/school/assessment-plans/campus/"
                                                + campusId
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.code")
                                .value("GT-TENANT-001")
                );

        org.mockito.Mockito.verifyNoInteractions(
                service
        );
    }


    @Test
    void rejectsUnauthenticatedAssessmentPlanCampusList()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.get(
                                        "/api/v1/school/assessment-plans/campus/"
                                                + campusId
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.code")
                                .value("GT-AUTH-001")
                );

        org.mockito.Mockito.verifyNoInteractions(
                service
        );
    }


    @Test
    void authenticatedReaderUsesJwtTenantForAssessmentPlanClassGradeList()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.read"
                        )
                );

        org.mockito.Mockito.when(
                service.findByClassGrade(
                        org.mockito.ArgumentMatchers.eq(
                                tenantId
                        ),
                        org.mockito.ArgumentMatchers.eq(
                                classGradeId
                        )
                )
        ).thenReturn(
                java.util.List.of()
        );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.get(
                                        "/api/v1/school/assessment-plans/grade/"
                                                + classGradeId
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                );

        org.mockito.Mockito.verify(
                service
        ).findByClassGrade(
                tenantId,
                classGradeId
        );
    }


    @Test
    void createPermissionCannotReadAssessmentPlansByClassGrade()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.create"
                        )
                );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.get(
                                        "/api/v1/school/assessment-plans/grade/"
                                                + classGradeId
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                );

        org.mockito.Mockito.verifyNoInteractions(
                service
        );
    }


    @Test
    void rejectsCrossTenantAssessmentPlanClassGradeListBeforeService()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        principalTenant,
                        Set.of(
                                "school.academic.assessment.read"
                        )
                );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.get(
                                        "/api/v1/school/assessment-plans/grade/"
                                                + classGradeId
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        requestedTenant.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.code")
                                .value("GT-TENANT-002")
                );

        org.mockito.Mockito.verifyNoInteractions(
                service
        );
    }


    @Test
    void rejectsAssessmentPlanClassGradeListWithoutTenantHeader()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of(
                                "school.academic.assessment.read"
                        )
                );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.get(
                                        "/api/v1/school/assessment-plans/grade/"
                                                + classGradeId
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.code")
                                .value("GT-TENANT-001")
                );

        org.mockito.Mockito.verifyNoInteractions(
                service
        );
    }


    @Test
    void rejectsUnauthenticatedAssessmentPlanClassGradeList()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.get(
                                        "/api/v1/school/assessment-plans/grade/"
                                                + classGradeId
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.code")
                                .value("GT-AUTH-001")
                );

        org.mockito.Mockito.verifyNoInteractions(
                service
        );
    }


    private String token(
            UUID userId,
            UUID tenantId,
            Set<String> permissions
    ) {

        GtPrincipal principal =
                new GtPrincipal(
                        userId,
                        "assessment-plan-test-user",
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
