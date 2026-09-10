package africa.growtogether.platform.school.academic.teaching;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/school/academic/teachers")
public class TeacherProfileController {

    private final TeacherProfileService service;
    private final ApiResponses responses;
    private final EnterpriseIdentityContext identity;

    public TeacherProfileController(
            TeacherProfileService service,
            ApiResponses responses,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.responses = responses;
        this.identity = identity;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('school.academic.teacher-profile.create')")
    public ApiResponse<TeacherProfile> create(
            @RequestParam UUID tenantId,
            @RequestParam UUID workforceMemberId,
            @RequestParam String teacherNumber,
            @RequestParam(required = false) String teacherRegistrationNumber,
            @RequestParam(required = false) String teachingLicenceNumber,
            @RequestParam(required = false) LocalDate teachingLicenceIssuedAt,
            @RequestParam(required = false) LocalDate teachingLicenceExpiresAt,
            @RequestParam(required = false) String highestTeachingLevel,
            @RequestParam(required = false) String primarySpecialization,
            @RequestParam(required = false) String secondarySpecialization,
            @RequestParam(required = false) String teacherCategory,
            @RequestParam(defaultValue = "false") boolean qualifiedForBoardingDuty,
            @RequestParam(defaultValue = "false") boolean qualifiedForSpecialNeeds,
            @RequestParam(defaultValue = "false") boolean qualifiedForCounselling,
            @RequestParam(required = false) Integer maximumWeeklyPeriods,
            @RequestParam(required = false) String notes
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-TEACHER-PROFILE-001",
                "Teacher profile created.",
                service.create(
                        tenantId,
                        workforceMemberId,
                        teacherNumber,
                        teacherRegistrationNumber,
                        teachingLicenceNumber,
                        teachingLicenceIssuedAt,
                        teachingLicenceExpiresAt,
                        highestTeachingLevel,
                        primarySpecialization,
                        secondarySpecialization,
                        teacherCategory,
                        qualifiedForBoardingDuty,
                        qualifiedForSpecialNeeds,
                        qualifiedForCounselling,
                        maximumWeeklyPeriods,
                        notes
                )
        );
    }

    @GetMapping("/{teacherNumber}")
    @PreAuthorize("hasAuthority('school.academic.teacher-profile.read')")
    public ApiResponse<TeacherProfile> getByTeacherNumber(
            @PathVariable String teacherNumber,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-TEACHER-PROFILE-002",
                "Teacher profile retrieved.",
                service.findByTeacherNumber(
                        tenantId,
                        teacherNumber
                )
        );
    }

    @GetMapping("/workforce/{workforceMemberId}")
    @PreAuthorize("hasAuthority('school.academic.teacher-profile.read')")
    public ApiResponse<TeacherProfile> getByWorkforceMember(
            @PathVariable UUID workforceMemberId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-TEACHER-PROFILE-003",
                "Teacher profile retrieved by workforce member.",
                service.findByWorkforceMember(
                        tenantId,
                        workforceMemberId
                )
        );
    }

    @GetMapping("/category/{teacherCategory}")
    @PreAuthorize("hasAuthority('school.academic.teacher-profile.read')")
    public ApiResponse<List<TeacherProfile>> findByCategory(
            @PathVariable String teacherCategory,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-TEACHER-PROFILE-004",
                "Teacher profiles retrieved by category.",
                service.findByCategory(
                        tenantId,
                        teacherCategory
                )
        );
    }

    @GetMapping("/status/{teachingStatus}")
    @PreAuthorize("hasAuthority('school.academic.teacher-profile.read')")
    public ApiResponse<List<TeacherProfile>> findByStatus(
            @PathVariable String teachingStatus,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return responses.success(
                "GT-SCHOOL-TEACHER-PROFILE-005",
                "Teacher profiles retrieved by teaching status.",
                service.findByTeachingStatus(
                        tenantId,
                        teachingStatus
                )
        );
    }

    @PatchMapping("/{teacherNumber}/activate")
    @PreAuthorize("hasAuthority('school.academic.teacher-profile.manage')")
    public ApiResponse<TeacherProfile> activate(
            @PathVariable String teacherNumber,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        TeacherProfile profile =
                service.findByTeacherNumber(
                        tenantId,
                        teacherNumber
                );

        return responses.success(
                "GT-SCHOOL-TEACHER-PROFILE-006",
                "Teacher profile activated.",
                service.activate(
                        profile
                )
        );
    }

    @PatchMapping("/{teacherNumber}/deactivate")
    @PreAuthorize("hasAuthority('school.academic.teacher-profile.manage')")
    public ApiResponse<TeacherProfile> deactivate(
            @PathVariable String teacherNumber,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        TeacherProfile profile =
                service.findByTeacherNumber(
                        tenantId,
                        teacherNumber
                );

        return responses.success(
                "GT-SCHOOL-TEACHER-PROFILE-007",
                "Teacher profile deactivated.",
                service.deactivate(
                        profile
                )
        );
    }
}
