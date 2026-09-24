package africa.growtogether.platform.school.learner.programme;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.school.enrollment.StudentEnrollment;
import africa.growtogether.platform.school.enrollment.StudentEnrollmentRepository;
import africa.growtogether.platform.school.learner.security.AuthenticatedLearnerResolver;
import africa.growtogether.platform.school.profile.SchoolProfileService;
import africa.growtogether.platform.school.student.Student;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LearnerProgrammeDayServiceTest {

    @Test
    void resolvesAuthenticatedLearnerAndCurrentEnrollment() {

        AuthenticatedLearnerResolver learners =
                mock(AuthenticatedLearnerResolver.class);

        StudentEnrollmentRepository enrollments =
                mock(StudentEnrollmentRepository.class);

        SchoolProfileService schools =
                mock(SchoolProfileService.class);

        Student student =
                mock(Student.class);

        StudentEnrollment enrollment =
                mock(StudentEnrollment.class);

        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID learnerId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();
        UUID academicYearId = UUID.randomUUID();
        UUID academicTermId = UUID.randomUUID();
        UUID campusId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();
        UUID streamId = UUID.randomUUID();

        when(student.getId())
                .thenReturn(learnerId);

        when(learners.requireCurrentLearner())
                .thenReturn(
                        new AuthenticatedLearnerResolver.ResolvedLearner(
                                tenantId,
                                userId,
                                student
                        )
                );

        when(
                enrollments
                        .findFirstByTenantIdAndStudentIdAndEnrollmentStatusAndStatusOrderByEnrollmentDateDesc(
                                tenantId,
                                learnerId,
                                "ACTIVE",
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                Optional.of(enrollment)
        );

        when(enrollment.getId())
                .thenReturn(enrollmentId);

        when(enrollment.getAcademicYearId())
                .thenReturn(academicYearId);

        when(enrollment.getAcademicTermId())
                .thenReturn(academicTermId);

        when(enrollment.getCampusId())
                .thenReturn(campusId);

        when(enrollment.getClassGradeId())
                .thenReturn(classGradeId);

        when(enrollment.getStreamId())
                .thenReturn(streamId);

        when(schools.requireTimezone(tenantId))
                .thenReturn(
                        ZoneId.of(
                                "Africa/Kampala"
                        )
                );

        Clock clock =
                Clock.fixed(
                        Instant.parse(
                                "2026-09-24T08:30:00Z"
                        ),
                        ZoneId.of("UTC")
                );

        LearnerProgrammeDayService service =
                new LearnerProgrammeDayService(
                        learners,
                        enrollments,
                        schools,
                        clock
                );

        LearnerProgrammeDayService.ProgrammeDay day =
                service.currentDay();

        assertThat(day.tenantId())
                .isEqualTo(tenantId);

        assertThat(day.learnerId())
                .isEqualTo(learnerId);

        assertThat(day.enrollmentId())
                .isEqualTo(enrollmentId);

        assertThat(day.academicYearId())
                .isEqualTo(academicYearId);

        assertThat(day.academicTermId())
                .isEqualTo(academicTermId);

        assertThat(day.campusId())
                .isEqualTo(campusId);

        assertThat(day.classGradeId())
                .isEqualTo(classGradeId);

        assertThat(day.streamId())
                .isEqualTo(streamId);

        assertThat(day.date())
                .hasToString("2026-09-24");

        assertThat(day.zone())
                .isEqualTo(
                        ZoneId.of(
                                "Africa/Kampala"
                        )
                );

        verify(
                enrollments
        ).findFirstByTenantIdAndStudentIdAndEnrollmentStatusAndStatusOrderByEnrollmentDateDesc(
                tenantId,
                learnerId,
                "ACTIVE",
                EntityStatus.ACTIVE
        );
    }

    @Test
    void rejectsLearnerWithoutActiveEnrollment() {

        AuthenticatedLearnerResolver learners =
                mock(AuthenticatedLearnerResolver.class);

        StudentEnrollmentRepository enrollments =
                mock(StudentEnrollmentRepository.class);

        SchoolProfileService schools =
                mock(SchoolProfileService.class);

        Student student =
                mock(Student.class);

        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID learnerId = UUID.randomUUID();

        when(student.getId())
                .thenReturn(learnerId);

        when(learners.requireCurrentLearner())
                .thenReturn(
                        new AuthenticatedLearnerResolver.ResolvedLearner(
                                tenantId,
                                userId,
                                student
                        )
                );

        when(
                enrollments
                        .findFirstByTenantIdAndStudentIdAndEnrollmentStatusAndStatusOrderByEnrollmentDateDesc(
                                tenantId,
                                learnerId,
                                "ACTIVE",
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                Optional.empty()
        );

        LearnerProgrammeDayService service =
                new LearnerProgrammeDayService(
                        learners,
                        enrollments,
                        schools,
                        Clock.systemUTC()
                );

        assertThatThrownBy(
                service::currentDay
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "no active enrollment"
                );
    }
}
