package africa.growtogether.platform.school.academic.curriculum;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping(
        "/api/v1/school/academic/curriculum/{curriculumVersionId}/subject-catalogue"
)
public class SubjectCatalogueController {


    private final SubjectCatalogueService service;
    private final ApiResponses responses;
    private final EnterpriseIdentityContext identity;


    public SubjectCatalogueController(
            SubjectCatalogueService service,
            ApiResponses responses,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.responses = responses;
        this.identity = identity;
    }


    @PostMapping
    @PreAuthorize("hasAuthority('school.academic.subject.create')")
    public ApiResponse<SubjectCatalogue> create(
            @PathVariable UUID curriculumVersionId,
            @RequestParam UUID tenantId,
            @RequestParam UUID learningAreaId,
            @RequestParam String subjectCode,
            @RequestParam String subjectName,
            @RequestParam String subjectType,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "1") Integer sequenceNumber
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-SUBJECT-CATALOGUE-001",
                "Subject catalogue entry created.",
                service.create(
                        tenantId,
                        curriculumVersionId,
                        learningAreaId,
                        subjectCode,
                        subjectName,
                        subjectType,
                        description,
                        sequenceNumber
                )
        );
    }


    @GetMapping
    @PreAuthorize("hasAuthority('school.academic.subject.read')")
    public ApiResponse<List<SubjectCatalogue>> list(
            @PathVariable UUID curriculumVersionId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-SUBJECT-CATALOGUE-002",
                "Subject catalogue entries retrieved.",
                service.findByCurriculumVersion(
                        tenantId,
                        curriculumVersionId
                )
        );
    }


    @GetMapping("/{code}")
    @PreAuthorize("hasAuthority('school.academic.subject.read')")
    public ApiResponse<SubjectCatalogue> get(
            @PathVariable UUID curriculumVersionId,
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-SUBJECT-CATALOGUE-003",
                "Subject catalogue entry retrieved.",
                service.findByCode(
                        tenantId,
                        curriculumVersionId,
                        code
                )
        );
    }


    @PatchMapping("/{code}/activate")
    @PreAuthorize("hasAuthority('school.academic.subject.manage')")
    public ApiResponse<SubjectCatalogue> activate(
            @PathVariable UUID curriculumVersionId,
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        SubjectCatalogue subject =
                service.findByCode(
                        tenantId,
                        curriculumVersionId,
                        code
                );

        return responses.success(
                "GT-SCHOOL-SUBJECT-CATALOGUE-004",
                "Subject catalogue entry activated.",
                service.activate(subject)
        );
    }


    @PatchMapping("/{code}/deactivate")
    @PreAuthorize("hasAuthority('school.academic.subject.manage')")
    public ApiResponse<SubjectCatalogue> deactivate(
            @PathVariable UUID curriculumVersionId,
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        SubjectCatalogue subject =
                service.findByCode(
                        tenantId,
                        curriculumVersionId,
                        code
                );

        return responses.success(
                "GT-SCHOOL-SUBJECT-CATALOGUE-005",
                "Subject catalogue entry deactivated.",
                service.deactivate(subject)
        );
    }
}
