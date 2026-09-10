package africa.growtogether.platform.school.assessment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Component
public class AssessmentPlanReferenceGateway {


    private static final String ACADEMIC_PROGRAMME_EXISTS_SQL = """
            SELECT EXISTS (
                SELECT 1
                FROM gts_academic_programme
                WHERE tenant_id = ?
                  AND id = ?
            )
            """;


    private static final String STUDY_TRACK_EXISTS_SQL = """
            SELECT EXISTS (
                SELECT 1
                FROM gts_study_track
                WHERE tenant_id = ?
                  AND id = ?
            )
            """;


    private static final String GRADING_SCHEME_EXISTS_SQL = """
            SELECT EXISTS (
                SELECT 1
                FROM gts_grading_scheme
                WHERE tenant_id = ?
                  AND id = ?
            )
            """;


    private static final String STUDY_TRACK_BELONGS_TO_PROGRAMME_SQL = """
            SELECT EXISTS (
                SELECT 1
                FROM gts_study_track
                WHERE tenant_id = ?
                  AND id = ?
                  AND academic_programme_id = ?
            )
            """;


    private final JdbcTemplate jdbcTemplate;


    public AssessmentPlanReferenceGateway(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Transactional(readOnly = true)
    public boolean academicProgrammeExists(
            UUID tenantId,
            UUID academicProgrammeId
    ) {

        return exists(
                ACADEMIC_PROGRAMME_EXISTS_SQL,
                tenantId,
                academicProgrammeId
        );
    }


    @Transactional(readOnly = true)
    public boolean studyTrackExists(
            UUID tenantId,
            UUID studyTrackId
    ) {

        return exists(
                STUDY_TRACK_EXISTS_SQL,
                tenantId,
                studyTrackId
        );
    }


    @Transactional(readOnly = true)
    public boolean gradingSchemeExists(
            UUID tenantId,
            UUID gradingSchemeId
    ) {

        return exists(
                GRADING_SCHEME_EXISTS_SQL,
                tenantId,
                gradingSchemeId
        );
    }


    @Transactional(readOnly = true)
    public boolean studyTrackBelongsToProgramme(
            UUID tenantId,
            UUID studyTrackId,
            UUID academicProgrammeId
    ) {

        Boolean result =
                jdbcTemplate.queryForObject(
                        STUDY_TRACK_BELONGS_TO_PROGRAMME_SQL,
                        Boolean.class,
                        tenantId,
                        studyTrackId,
                        academicProgrammeId
                );

        return Boolean.TRUE.equals(result);
    }


    private boolean exists(
            String sql,
            UUID tenantId,
            UUID referenceId
    ) {

        Boolean result =
                jdbcTemplate.queryForObject(
                        sql,
                        Boolean.class,
                        tenantId,
                        referenceId
                );

        return Boolean.TRUE.equals(result);
    }
}
