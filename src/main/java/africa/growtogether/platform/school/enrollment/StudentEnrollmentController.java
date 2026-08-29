package africa.growtogether.platform.school.enrollment;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/school/enrollments")
public class StudentEnrollmentController {

    private final StudentEnrollmentService service;

    public StudentEnrollmentController(
            StudentEnrollmentService service
    ) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize(
            "hasAuthority('school.enrollment.create')"
    )
    public ResponseEntity<StudentEnrollment> create(
            @RequestParam UUID tenantId,
            @RequestParam UUID studentId,
            @RequestParam UUID academicYearId,
            @RequestParam(required = false) UUID academicTermId,
            @RequestParam UUID campusId,
            @RequestParam UUID classGradeId,
            @RequestParam(required = false) UUID streamId,
            @RequestParam String enrollmentNumber,
            @RequestParam LocalDate enrollmentDate,
            @RequestParam LocalDate effectiveFrom,
            @RequestParam(required = false) LocalDate effectiveTo,
            @RequestParam(required = false) String enrollmentType,
            @RequestParam(required = false) UUID previousEnrollmentId,
            @RequestParam(required = false) UUID workflowInstanceId,
            @RequestParam(required = false) UUID enrolledBy
    ) {

        return ResponseEntity.ok(
                service.create(
                        tenantId,
                        studentId,
                        academicYearId,
                        academicTermId,
                        campusId,
                        classGradeId,
                        streamId,
                        enrollmentNumber,
                        enrollmentDate,
                        effectiveFrom,
                        effectiveTo,
                        enrollmentType,
                        previousEnrollmentId,
                        workflowInstanceId,
                        enrolledBy
                )
        );
    }

    @GetMapping("/{enrollmentId}")
    @PreAuthorize(
            "hasAuthority('school.enrollment.read')"
    )
    public ResponseEntity<StudentEnrollment> getById(
            @PathVariable UUID enrollmentId,
            @RequestParam UUID tenantId
    ) {

        return ResponseEntity.ok(
                service.findById(
                        tenantId,
                        enrollmentId
                )
        );
    }

    @GetMapping("/number/{enrollmentNumber}")
    @PreAuthorize(
            "hasAuthority('school.enrollment.read')"
    )
    public ResponseEntity<StudentEnrollment> getByEnrollmentNumber(
            @PathVariable String enrollmentNumber,
            @RequestParam UUID tenantId
    ) {

        return ResponseEntity.ok(
                service.findByEnrollmentNumber(
                        tenantId,
                        enrollmentNumber
                )
        );
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize(
            "hasAuthority('school.enrollment.read')"
    )
    public ResponseEntity<List<StudentEnrollment>> findByStudent(
            @PathVariable UUID studentId,
            @RequestParam UUID tenantId
    ) {

        return ResponseEntity.ok(
                service.findByStudent(
                        tenantId,
                        studentId
                )
        );
    }

    @GetMapping("/academic-year/{academicYearId}")
    @PreAuthorize(
            "hasAuthority('school.enrollment.read')"
    )
    public ResponseEntity<List<StudentEnrollment>> findByAcademicYear(
            @PathVariable UUID academicYearId,
            @RequestParam UUID tenantId
    ) {

        return ResponseEntity.ok(
                service.findByAcademicYear(
                        tenantId,
                        academicYearId
                )
        );
    }

    @GetMapping("/campus/{campusId}")
    @PreAuthorize(
            "hasAuthority('school.enrollment.read')"
    )
    public ResponseEntity<List<StudentEnrollment>> findByCampus(
            @PathVariable UUID campusId,
            @RequestParam UUID tenantId
    ) {

        return ResponseEntity.ok(
                service.findByCampus(
                        tenantId,
                        campusId
                )
        );
    }

    @GetMapping("/class-grade/{classGradeId}")
    @PreAuthorize(
            "hasAuthority('school.enrollment.read')"
    )
    public ResponseEntity<List<StudentEnrollment>> findByClassGrade(
            @PathVariable UUID classGradeId,
            @RequestParam UUID tenantId
    ) {

        return ResponseEntity.ok(
                service.findByClassGrade(
                        tenantId,
                        classGradeId
                )
        );
    }

    @GetMapping("/stream/{streamId}")
    @PreAuthorize(
            "hasAuthority('school.enrollment.read')"
    )
    public ResponseEntity<List<StudentEnrollment>> findByStream(
            @PathVariable UUID streamId,
            @RequestParam UUID tenantId
    ) {

        return ResponseEntity.ok(
                service.findByStream(
                        tenantId,
                        streamId
                )
        );
    }

    @GetMapping("/status/{enrollmentStatus}")
    @PreAuthorize(
            "hasAuthority('school.enrollment.read')"
    )
    public ResponseEntity<List<StudentEnrollment>> findByStatus(
            @PathVariable String enrollmentStatus,
            @RequestParam UUID tenantId
    ) {

        return ResponseEntity.ok(
                service.findByStatus(
                        tenantId,
                        enrollmentStatus
                )
        );
    }

    @PatchMapping("/{enrollmentId}/pending")
    @PreAuthorize(
            "hasAuthority('school.enrollment.manage')"
    )
    public ResponseEntity<StudentEnrollment> markPending(
            @PathVariable UUID enrollmentId,
            @RequestParam UUID tenantId
    ) {

        return ResponseEntity.ok(
                service.markPending(
                        tenantId,
                        enrollmentId
                )
        );
    }

    @PatchMapping("/{enrollmentId}/activate")
    @PreAuthorize(
            "hasAuthority('school.enrollment.manage')"
    )
    public ResponseEntity<StudentEnrollment> activate(
            @PathVariable UUID enrollmentId,
            @RequestParam UUID tenantId,
            @RequestParam UUID approvedBy
    ) {

        return ResponseEntity.ok(
                service.activate(
                        tenantId,
                        enrollmentId,
                        approvedBy
                )
        );
    }

    @PatchMapping("/{enrollmentId}/suspend")
    @PreAuthorize(
            "hasAuthority('school.enrollment.manage')"
    )
    public ResponseEntity<StudentEnrollment> suspend(
            @PathVariable UUID enrollmentId,
            @RequestParam UUID tenantId
    ) {

        return ResponseEntity.ok(
                service.suspend(
                        tenantId,
                        enrollmentId
                )
        );
    }

    @PatchMapping("/{enrollmentId}/complete")
    @PreAuthorize(
            "hasAuthority('school.enrollment.manage')"
    )
    public ResponseEntity<StudentEnrollment> complete(
            @PathVariable UUID enrollmentId,
            @RequestParam UUID tenantId,
            @RequestParam(required = false) LocalDate effectiveTo
    ) {

        return ResponseEntity.ok(
                service.complete(
                        tenantId,
                        enrollmentId,
                        effectiveTo
                )
        );
    }

    @PatchMapping("/{enrollmentId}/withdraw")
    @PreAuthorize(
            "hasAuthority('school.enrollment.manage')"
    )
    public ResponseEntity<StudentEnrollment> withdraw(
            @PathVariable UUID enrollmentId,
            @RequestParam UUID tenantId,
            @RequestParam LocalDate exitDate,
            @RequestParam(required = false) String exitReason
    ) {

        return ResponseEntity.ok(
                service.withdraw(
                        tenantId,
                        enrollmentId,
                        exitDate,
                        exitReason
                )
        );
    }

    @PatchMapping("/{enrollmentId}/transfer")
    @PreAuthorize(
            "hasAuthority('school.enrollment.manage')"
    )
    public ResponseEntity<StudentEnrollment> transfer(
            @PathVariable UUID enrollmentId,
            @RequestParam UUID tenantId,
            @RequestParam LocalDate exitDate,
            @RequestParam(required = false) String exitReason
    ) {

        return ResponseEntity.ok(
                service.transfer(
                        tenantId,
                        enrollmentId,
                        exitDate,
                        exitReason
                )
        );
    }

    @PatchMapping("/{enrollmentId}/cancel")
    @PreAuthorize(
            "hasAuthority('school.enrollment.manage')"
    )
    public ResponseEntity<StudentEnrollment> cancel(
            @PathVariable UUID enrollmentId,
            @RequestParam UUID tenantId
    ) {

        return ResponseEntity.ok(
                service.cancel(
                        tenantId,
                        enrollmentId
                )
        );
    }
}
