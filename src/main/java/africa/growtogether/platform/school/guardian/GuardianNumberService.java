package africa.growtogether.platform.school.guardian;

import africa.growtogether.platform.school.profile.SchoolProfile;
import africa.growtogether.platform.school.profile.SchoolProfileService;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class GuardianNumberService {

    private static final String NEXT_SEQUENCE_SQL = """
            INSERT INTO gts_guardian_number_sequence (
                tenant_id,
                last_issued_number
            )
            VALUES (?, 1)
            ON CONFLICT (tenant_id)
            DO UPDATE
            SET last_issued_number =
                    gts_guardian_number_sequence.last_issued_number + 1,
                updated_at = CURRENT_TIMESTAMP
            RETURNING last_issued_number
            """;

    private final JdbcTemplate jdbcTemplate;
    private final SchoolProfileService schoolProfiles;

    public GuardianNumberService(
            JdbcTemplate jdbcTemplate,
            SchoolProfileService schoolProfiles
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.schoolProfiles = schoolProfiles;
    }

    @Transactional
    public String next(
            UUID tenantId
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        SchoolProfile schoolProfile =
                schoolProfiles.getForTenant(
                        tenantId
                );

        String schoolCode =
                normalizeSchoolCode(
                        schoolProfile.getSchoolCode()
                );

        Long sequence =
                jdbcTemplate.queryForObject(
                        NEXT_SEQUENCE_SQL,
                        Long.class,
                        tenantId
                );

        if (
                sequence == null
                || sequence <= 0
        ) {
            throw new IllegalStateException(
                    "Unable to generate guardian sequence"
            );
        }

        return "%s-GDN-%06d".formatted(
                schoolCode,
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
                    "School code is invalid for guardian numbering"
            );
        }

        return normalized;
    }
}
