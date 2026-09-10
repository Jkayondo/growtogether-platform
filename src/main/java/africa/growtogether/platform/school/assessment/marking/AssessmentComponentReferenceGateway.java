package africa.growtogether.platform.school.assessment.marking;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Component
public class AssessmentComponentReferenceGateway {


    private final JdbcTemplate jdbcTemplate;


    public AssessmentComponentReferenceGateway(
            JdbcTemplate jdbcTemplate
    ) {

        this.jdbcTemplate = jdbcTemplate;
    }


    @Transactional(readOnly = true)
    public Optional<AssessmentComponentReference> find(
            UUID tenantId,
            UUID assessmentComponentId
    ) {

        if (
                tenantId == null
                || assessmentComponentId == null
        ) {

            return Optional.empty();
        }


        List<AssessmentComponentReference> rows =
                jdbcTemplate.query(
                        """
                        SELECT
                            id,
                            assessment_plan_id,
                            assessment_type_id,
                            subject_offering_id,
                            maximum_score,
                            pass_score,
                            component_status,
                            status
                        FROM gts_assessment_component
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        (resultSet, rowNumber) ->
                                new AssessmentComponentReference(
                                        resultSet.getObject(
                                                "id",
                                                UUID.class
                                        ),
                                        resultSet.getObject(
                                                "assessment_plan_id",
                                                UUID.class
                                        ),
                                        resultSet.getObject(
                                                "assessment_type_id",
                                                UUID.class
                                        ),
                                        resultSet.getObject(
                                                "subject_offering_id",
                                                UUID.class
                                        ),
                                        resultSet.getBigDecimal(
                                                "maximum_score"
                                        ),
                                        resultSet.getBigDecimal(
                                                "pass_score"
                                        ),
                                        resultSet.getString(
                                                "component_status"
                                        ),
                                        resultSet.getString(
                                                "status"
                                        )
                                ),
                        tenantId,
                        assessmentComponentId
                );


        return rows.stream()
                .findFirst();
    }


    public record AssessmentComponentReference(

            UUID id,

            UUID assessmentPlanId,

            UUID assessmentTypeId,

            UUID subjectOfferingId,

            BigDecimal maximumScore,

            BigDecimal passScore,

            String componentStatus,

            String status

    ) {
    }
}
