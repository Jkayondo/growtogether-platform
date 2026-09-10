package africa.growtogether.platform.school.academic.teaching;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.connect.ConnectTeacherAuthorizationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/school/academic/teaching-assignments/me")
public class CurrentTeacherAssignmentController {
    private final EnterpriseIdentityContext identity;
    private final ConnectTeacherAuthorizationService teachers;
    private final TeachingAssignmentService assignments;
    private final ApiResponses responses;

    public CurrentTeacherAssignmentController(
            EnterpriseIdentityContext identity,
            ConnectTeacherAuthorizationService teachers,
            TeachingAssignmentService assignments,
            ApiResponses responses
    ) {
        this.identity = identity;
        this.teachers = teachers;
        this.assignments = assignments;
        this.responses = responses;
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('school.academic.teaching-assignment.read')")
    public ApiResponse<List<TeachingAssignment>> active() {
        UUID tenantId = identity.requireTenantId();
        TeacherProfile teacher = teachers.requireUniqueCurrentTeacherProfile();

        List<TeachingAssignment> result =
                assignments.findByTeacher(tenantId, teacher.getId())
                        .stream()
                        .filter(item -> "ACTIVE".equals(item.getAssignmentStatus()))
                        .toList();

        return responses.success(
                "GT-SCHOOL-TEACHING-ASSIGNMENT-ME-001",
                "Active teaching assignments retrieved for the authenticated teacher.",
                result
        );
    }
}
