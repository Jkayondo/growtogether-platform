package africa.growtogether.platform.school.assessment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface AssessmentPlanRepository
        extends JpaRepository<AssessmentPlan, UUID> {


    Optional<AssessmentPlan> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );


    Optional<AssessmentPlan> findByTenantIdAndPlanCode(
            UUID tenantId,
            String planCode
    );


    boolean existsByTenantIdAndPlanCode(
            UUID tenantId,
            String planCode
    );


    List<AssessmentPlan> findByTenantIdAndAcademicYearId(
            UUID tenantId,
            UUID academicYearId
    );


    List<AssessmentPlan> findByTenantIdAndCampusId(
            UUID tenantId,
            UUID campusId
    );


    List<AssessmentPlan> findByTenantIdAndClassGradeId(
            UUID tenantId,
            UUID classGradeId
    );

    @Query("""
            select count(p)
            from AssessmentPlan p
            where p.tenantId = :tenantId
              and p.academicYearId = :academicYearId
              and (
                    p.academicTermId = :academicTermId
                    or (
                        p.academicTermId is null
                        and :academicTermId is null
                    )
              )
              and p.campusId = :campusId
              and (
                    p.academicProgrammeId = :academicProgrammeId
                    or (
                        p.academicProgrammeId is null
                        and :academicProgrammeId is null
                    )
              )
              and (
                    p.studyTrackId = :studyTrackId
                    or (
                        p.studyTrackId is null
                        and :studyTrackId is null
                    )
              )
              and p.classGradeId = :classGradeId
              and (
                    p.streamId = :streamId
                    or (
                        p.streamId is null
                        and :streamId is null
                    )
              )
            """)
    long countByTenantAndAcademicScope(
            @Param("tenantId") UUID tenantId,
            @Param("academicYearId") UUID academicYearId,
            @Param("academicTermId") UUID academicTermId,
            @Param("campusId") UUID campusId,
            @Param("academicProgrammeId") UUID academicProgrammeId,
            @Param("studyTrackId") UUID studyTrackId,
            @Param("classGradeId") UUID classGradeId,
            @Param("streamId") UUID streamId
    );
}
