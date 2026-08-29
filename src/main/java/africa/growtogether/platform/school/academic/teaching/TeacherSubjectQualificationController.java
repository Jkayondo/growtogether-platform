package africa.growtogether.platform.school.academic.teaching;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/school/academic/teacher-subject-qualifications")
public class TeacherSubjectQualificationController {

    private final TeacherSubjectQualificationService service;
    private final ApiResponses responses;

    public TeacherSubjectQualificationController(
            TeacherSubjectQualificationService service,
            ApiResponses responses
    ) {
        this.service = service;
        this.responses = responses;
    }

    @PostMapping
    @PreAuthorize(
            "hasAuthority('school.academic.teacher-subject-qualification.create')"
    )
    public ApiResponse<TeacherSubjectQualification> create(
            @RequestParam UUID tenantId,
            @RequestParam UUID teacherProfileId,
            @RequestParam UUID subjectId,
            @RequestParam(required = false) String competencyLevel,
            @RequestParam(defaultValue = "false") boolean primarySubject,
            @RequestParam(required = false) UUID minimumClassGradeId,
            @RequestParam(required = false) UUID maximumClassGradeId,
            @RequestParam(required = false) LocalDate effectiveFrom,
            @RequestParam(required = false) LocalDate effectiveTo,
            @RequestParam(required = false) UUID edsEvidenceDocumentId
    ) {

        return responses.success(
                "GT-SCHOOL-TEACHER-SUBJECT-QUALIFICATION-001",
                "Teacher subject qualification created.",
                service.create(
                        tenantId,
                        teacherProfileId,
                        subjectId,
                        competencyLevel,
                        primarySubject,
                        minimumClassGradeId,
                        maximumClassGradeId,
                        effectiveFrom,
                        effectiveTo,
                        edsEvidenceDocumentId
                )
        );
    }

    @GetMapping("/{qualificationId}")
    @PreAuthorize(
            "hasAuthority('school.academic.teacher-subject-qualification.read')"
    )
    public ApiResponse<TeacherSubjectQualification> get(
            @PathVariable UUID qualificationId,
            @RequestParam UUID tenantId
    ) {

        return responses.success(
                "GT-SCHOOL-TEACHER-SUBJECT-QUALIFICATION-002",
                "Teacher subject qualification retrieved.",
                service.findById(
                        tenantId,
                        qualificationId
                )
        );
    }

    @GetMapping("/teacher/{teacherProfileId}")
    @PreAuthorize(
            "hasAuthority('school.academic.teacher-subject-qualification.read')"
    )
    public ApiResponse<List<TeacherSubjectQualification>> findByTeacher(
            @PathVariable UUID teacherProfileId,
            @RequestParam UUID tenantId
    ) {

        return responses.success(
                "GT-SCHOOL-TEACHER-SUBJECT-QUALIFICATION-003",
                "Teacher subject qualifications retrieved by teacher.",
                service.findByTeacher(
                        tenantId,
                        teacherProfileId
                )
        );
    }

    @GetMapping("/subject/{subjectId}")
    @PreAuthorize(
            "hasAuthority('school.academic.teacher-subject-qualification.read')"
    )
    public ApiResponse<List<TeacherSubjectQualification>> findBySubject(
            @PathVariable UUID subjectId,
            @RequestParam UUID tenantId
    ) {

        return responses.success(
                "GT-SCHOOL-TEACHER-SUBJECT-QUALIFICATION-004",
                "Teacher subject qualifications retrieved by subject.",
                service.findBySubject(
                        tenantId,
                        subjectId
                )
        );
    }

    @GetMapping("/verification-status/{verificationStatus}")
    @PreAuthorize(
            "hasAuthority('school.academic.teacher-subject-qualification.read')"
    )
    public ApiResponse<List<TeacherSubjectQualification>>
    findByVerificationStatus(
            @PathVariable String verificationStatus,
            @RequestParam UUID tenantId
    ) {

        return responses.success(
                "GT-SCHOOL-TEACHER-SUBJECT-QUALIFICATION-005",
                "Teacher subject qualifications retrieved by verification status.",
                service.findByVerificationStatus(
                        tenantId,
                        verificationStatus
                )
        );
    }

    @PatchMapping("/{qualificationId}/verify")
    @PreAuthorize(
            "hasAuthority('school.academic.teacher-subject-qualification.manage')"
    )
    public ApiResponse<TeacherSubjectQualification> verify(
            @PathVariable UUID qualificationId,
            @RequestParam UUID tenantId,
            @RequestParam UUID verifiedBy
    ) {

        return responses.success(
                "GT-SCHOOL-TEACHER-SUBJECT-QUALIFICATION-006",
                "Teacher subject qualification verified.",
                service.verify(
                        tenantId,
                        qualificationId,
                        verifiedBy
                )
        );
    }

    @PatchMapping("/{qualificationId}/reject")
    @PreAuthorize(
            "hasAuthority('school.academic.teacher-subject-qualification.manage')"
    )
    public ApiResponse<TeacherSubjectQualification> reject(
            @PathVariable UUID qualificationId,
            @RequestParam UUID tenantId
    ) {

        return responses.success(
                "GT-SCHOOL-TEACHER-SUBJECT-QUALIFICATION-007",
                "Teacher subject qualification rejected.",
                service.reject(
                        tenantId,
                        qualificationId
                )
        );
    }
}
