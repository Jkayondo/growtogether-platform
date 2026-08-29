package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.school.academic.year.AcademicYear;
import africa.growtogether.platform.school.academic.year.AcademicYearRepository;
import africa.growtogether.platform.school.profile.SchoolProfile;
import africa.growtogether.platform.school.profile.SchoolProfileService;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class AdmissionNumberService {

    private static final String NEXT_SEQUENCE_SQL = """
            INSERT INTO gts_admission_number_sequence (
                tenant_id,
                academic_year_id,
                last_issued_number
            )
            VALUES (?, ?, 1)
            ON CONFLICT (tenant_id, academic_year_id)
            DO UPDATE
            SET last_issued_number =
                    gts_admission_number_sequence.last_issued_number + 1,
                updated_at = CURRENT_TIMESTAMP
            RETURNING last_issued_number
            """;

    private final JdbcTemplate jdbcTemplate;
    private final SchoolProfileService schoolProfiles;
    private final AcademicYearRepository academicYears;

    public AdmissionNumberService(
            JdbcTemplate jdbcTemplate,
            SchoolProfileService schoolProfiles,
            AcademicYearRepository academicYears
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.schoolProfiles = schoolProfiles;
        this.academicYears = academicYears;
    }

    @Transactional
    public String next(
            UUID tenantId,
            UUID academicYearId
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (academicYearId == null) {
            throw new IllegalArgumentException(
                    "academicYearId must not be null"
            );
        }

        SchoolProfile schoolProfile =
                schoolProfiles.getForTenant(
                        tenantId
                );

        AcademicYear academicYear =
                academicYears
                        .findByTenantIdAndId(
                                tenantId,
                                academicYearId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Academic year not found for tenant"
                                )
                        );

        if (academicYear.getStartDate() == null) {
            throw new IllegalStateException(
                    "Academic year start date is required"
            );
        }

        String schoolCode =
                normalizeSchoolCode(
                        schoolProfile.getSchoolCode()
                );

        int admissionYear =
                academicYear
                        .getStartDate()
                        .getYear();

        Long sequence =
                jdbcTemplate.queryForObject(
                        NEXT_SEQUENCE_SQL,
                        Long.class,
                        tenantId,
                        academicYearId
                );

        if (
                sequence == null
                || sequence <= 0
        ) {
            throw new IllegalStateException(
                    "Unable to generate admission sequence"
            );
        }

        return "%s-%d-ADM-%06d".formatted(
                schoolCode,
                admissionYear,
                sequence
        );
    }

    private String normalizeSchoolCode(
            String schoolCode
    ) {

        if (
                schoolCode == null
                || schoolCode.isBlank()
        ) {
            throw new IllegalStateException(
                    "School code is not configured"
            );
        }

        String normalized =
                schoolCode
                        .trim()
                        .toUpperCase(Locale.ROOT)
                        .replaceAll(
                                "[^A-Z0-9]+",
                                "-"
                        )
                        .replaceAll(
                                "^-+|-+$",
                                ""
                        );

        if (normalized.isBlank()) {
            throw new IllegalStateException(
                    "School code is invalid for admission numbering"
            );
        }

        return normalized;
    }
}
