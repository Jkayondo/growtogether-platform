package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/academic/campuses")
public class CampusController {


    private final CampusService service;
    private final ApiResponses responses;
    private final EnterpriseIdentityContext identity;


    public CampusController(
            CampusService service,
            ApiResponses responses,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.responses = responses;
        this.identity = identity;
    }



    @PostMapping
    @PreAuthorize("hasAuthority('school.academic.campus.create')")
    public ApiResponse<Campus> create(
            @RequestParam UUID tenantId,
            @RequestParam UUID schoolProfileId,
            @RequestParam String campusCode,
            @RequestParam String campusName,
            @RequestParam(required = false) String addressLine,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "false") boolean mainCampus
    ) {

        identity.requireTenant(
                tenantId
        );

        return responses.success(
                "GT-SCHOOL-CAMPUS-001",
                "Campus created.",
                service.create(
                        tenantId,
                        schoolProfileId,
                        campusCode,
                        campusName,
                        addressLine,
                        district,
                        city,
                        countryCode,
                        phoneNumber,
                        email,
                        mainCampus
                )
        );

    }



    @GetMapping
    @PreAuthorize("hasAuthority('school.academic.campus.read')")
    public ApiResponse<List<Campus>> findBySchoolProfile(
            @RequestParam UUID tenantId,
            @RequestParam UUID schoolProfileId
    ) {

        identity.requireTenant(
                tenantId
        );

        return responses.success(
                "GT-SCHOOL-CAMPUS-002",
                "Campuses retrieved.",
                service.findBySchoolProfile(
                        tenantId,
                        schoolProfileId
                )
        );

    }



    @GetMapping("/{code}")
    @PreAuthorize("hasAuthority('school.academic.campus.read')")
    public ApiResponse<Campus> findByCode(
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );

        return responses.success(
                "GT-SCHOOL-CAMPUS-003",
                "Campus retrieved.",
                service.findByCode(
                        tenantId,
                        code
                )
        );

    }



    @PatchMapping("/{code}/activate")
    @PreAuthorize("hasAuthority('school.academic.campus.manage')")
    public ApiResponse<Campus> activate(
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );

        Campus campus =
                service.findByCode(
                        tenantId,
                        code
                );


        return responses.success(
                "GT-SCHOOL-CAMPUS-004",
                "Campus activated.",
                service.activate(campus)
        );

    }



    @PatchMapping("/{code}/deactivate")
    @PreAuthorize("hasAuthority('school.academic.campus.manage')")
    public ApiResponse<Campus> deactivate(
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );

        Campus campus =
                service.findByCode(
                        tenantId,
                        code
                );


        return responses.success(
                "GT-SCHOOL-CAMPUS-005",
                "Campus deactivated.",
                service.deactivate(campus)
        );

    }

}
