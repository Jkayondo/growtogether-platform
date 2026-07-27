package africa.growtogether.platform.school.academic.term;


import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.school.academic.year.AcademicYear;
import africa.growtogether.platform.school.academic.year.AcademicYearRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/academic/terms")
public class AcademicTermController {


    private final AcademicTermService service;

    private final AcademicYearRepository academicYearRepository;

    private final ApiResponses responses;


    public AcademicTermController(
            AcademicTermService service,
            AcademicYearRepository academicYearRepository,
            ApiResponses responses
    ) {
        this.service = service;
        this.academicYearRepository = academicYearRepository;
        this.responses = responses;
    }


    @PostMapping
    @PreAuthorize("hasAuthority('school.academic.term.create')")
    public ApiResponse<AcademicTerm> create(
            @RequestParam UUID tenantId,
            @RequestParam UUID academicYearId,
            @RequestParam String termCode,
            @RequestParam String termName,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam Integer sequenceNumber
    ) {

        AcademicYear academicYear =
                academicYearRepository.findById(
                        academicYearId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Academic year not found"
                        )
                );


        AcademicTerm term =
                service.create(
                        tenantId,
                        academicYear,
                        termCode,
                        termName,
                        startDate,
                        endDate,
                        sequenceNumber
                );


        return responses.success(
                "GT-SCHOOL-TERM-001",
                "Academic term created.",
                term
        );
    }


    @GetMapping("/academic-year/{academicYearId}")
    @PreAuthorize("hasAuthority('school.academic.term.read')")
    public ApiResponse<List<AcademicTerm>> byAcademicYear(
            @PathVariable UUID academicYearId
    ) {

        return responses.success(
                "GT-SCHOOL-TERM-002",
                "Academic terms retrieved.",
                service.findByAcademicYear(
                        academicYearId
                )
        );
    }


    @GetMapping("/academic-year/{academicYearId}/active")
    @PreAuthorize("hasAuthority('school.academic.term.read')")
    public ApiResponse<List<AcademicTerm>> active(
            @PathVariable UUID academicYearId
    ) {

        return responses.success(
                "GT-SCHOOL-TERM-003",
                "Active academic terms retrieved.",
                service.findActiveTerms(
                        academicYearId
                )
        );
    }

}
