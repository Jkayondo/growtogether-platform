package africa.growtogether.platform.school.learner.programme;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.school.enrollment.StudentEnrollment;
import africa.growtogether.platform.school.enrollment.StudentEnrollmentRepository;
import africa.growtogether.platform.school.learner.security.AuthenticatedLearnerResolver;
import africa.growtogether.platform.school.profile.SchoolProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class LearnerProgrammeDayService {

    private final AuthenticatedLearnerResolver learners;
    private final StudentEnrollmentRepository enrollments;
    private final SchoolProfileService schools;
    private final Clock clock;

    @Autowired
    public LearnerProgrammeDayService(
            AuthenticatedLearnerResolver learners,
            StudentEnrollmentRepository enrollments,
            SchoolProfileService schools
    ) {
        this(
                learners,
                enrollments,
                schools,
                Clock.systemUTC()
        );
    }

    LearnerProgrammeDayService(
            AuthenticatedLearnerResolver learners,
            StudentEnrollmentRepository enrollments,
            SchoolProfileService schools,
            Clock clock
    ) {
        this.learners = learners;
        this.enrollments = enrollments;
        this.schools = schools;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ProgrammeDay currentDay() {

        AuthenticatedLearnerResolver.ResolvedLearner learner =
                learners.requireCurrentLearner();

        UUID tenantId = learner.tenantId();
        UUID learnerId = learner.student().getId();

        StudentEnrollment enrollment =
                enrollments
                        .findFirstByTenantIdAndStudentIdAndEnrollmentStatusAndStatusOrderByEnrollmentDateDesc(
                                tenantId,
                                learnerId,
                                "ACTIVE",
                                EntityStatus.ACTIVE
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "Authenticated learner has no active enrollment."
                                )
                        );

        ZoneId zone =
                schools.requireTimezone(
                        tenantId
                );

        LocalDate date =
                LocalDate.now(
                        clock.withZone(
                                zone
                        )
                );

        return new ProgrammeDay(
                tenantId,
                learnerId,
                enrollment.getId(),
                enrollment.getAcademicYearId(),
                enrollment.getAcademicTermId(),
                enrollment.getCampusId(),
                enrollment.getClassGradeId(),
                enrollment.getStreamId(),
                date,
                zone,
                date.atStartOfDay(zone).toInstant(),
                date.plusDays(1)
                        .atStartOfDay(zone)
                        .toInstant()
        );
    }

    public record ProgrammeDay(
            UUID tenantId,
            UUID learnerId,
            UUID enrollmentId,
            UUID academicYearId,
            UUID academicTermId,
            UUID campusId,
            UUID classGradeId,
            UUID streamId,
            LocalDate date,
            ZoneId zone,
            Instant startInclusive,
            Instant endExclusive
    ) {
        public ProgrammeDay {
            if (
                    tenantId == null
                    || learnerId == null
                    || enrollmentId == null
                    || academicYearId == null
                    || campusId == null
                    || classGradeId == null
                    || date == null
                    || zone == null
                    || startInclusive == null
                    || endExclusive == null
            ) {
                throw new IllegalArgumentException(
                        "Learner programme day context is incomplete."
                );
            }
        }
    }
}
