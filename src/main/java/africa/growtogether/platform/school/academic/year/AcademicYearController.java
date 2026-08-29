package africa.growtogether.platform.school.academic.year;


import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/academic/years")
public class AcademicYearController {


    private final AcademicYearService service;

    private final ApiResponses responses;

    private final EnterpriseIdentityContext identity;


    public AcademicYearController(
            AcademicYearService service,
            ApiResponses responses,
            EnterpriseIdentityContext identity
    ) {

        this.service = service;
        this.responses = responses;
        this.identity = identity;

    }


    @PostMapping
    @PreAuthorize("hasAuthority('school.academic.year.create')")
    public ApiResponse<AcademicYear> create(
            @RequestParam UUID tenantId,
            @Valid @RequestBody CreateAcademicYearCommand command
    ) {

        identity.requireTenant(
                tenantId
        );

        AcademicYear year =
                service.create(
                        tenantId,
                        command
                );


        return responses.success(
                "GT-SCHOOL-YEAR-001",
                "Academic year created.",
                year
        );

    }


    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasAuthority('school.academic.year.read')")
    public ApiResponse<List<AcademicYear>> byTenant(
            @PathVariable UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );

        return responses.success(
                "GT-SCHOOL-YEAR-002",
                "Academic years retrieved.",
                service.findByTenant(
                        tenantId
                )
        );

    }

}