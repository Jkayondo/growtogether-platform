package africa.growtogether.platform.school.learner.programme;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.school.timetable.entry.TimetableEntry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LearnerProgrammeLessonRepository
        extends Repository<TimetableEntry, UUID> {

    @Query("""
        select e
        from TimetableEntry e, Timetable t
        where e.timetableId = t.id
          and e.tenantId = :tenantId
          and t.tenantId = :tenantId
          and t.academicYearId = :academicYearId
          and (
                :academicTermId is null
                or t.academicTermId = :academicTermId
          )
          and t.campusId = :campusId
          and e.classGradeId = :classGradeId
          and (
                (
                    :streamId is null
                    and e.streamId is null
                )
                or
                (
                    :streamId is not null
                    and (
                        e.streamId is null
                        or e.streamId = :streamId
                    )
                )
          )
          and e.status = :activeStatus
          and t.status = :activeStatus
          and e.entryType = 'LESSON'
          and e.entryStatus in ('SCHEDULED', 'CONFIRMED', 'ACTIVE')
          and t.timetableStatus in ('PUBLISHED', 'ACTIVE')
          and e.dayOfWeek = :dayOfWeek
          and t.effectiveFrom <= :date
          and (t.effectiveTo is null or t.effectiveTo >= :date)
          and (e.effectiveFrom is null or e.effectiveFrom <= :date)
          and (e.effectiveTo is null or e.effectiveTo >= :date)
        order by e.timetableId, e.bellPeriodId, e.id
        """)
    List<TimetableEntry> findLessonCandidates(
            @Param("tenantId") UUID tenantId,
            @Param("academicYearId") UUID academicYearId,
            @Param("academicTermId") UUID academicTermId,
            @Param("campusId") UUID campusId,
            @Param("classGradeId") UUID classGradeId,
            @Param("streamId") UUID streamId,
            @Param("date") LocalDate date,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("activeStatus") EntityStatus activeStatus
    );
}
