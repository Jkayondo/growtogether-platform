package africa.growtogether.platform.school.programme;

import africa.growtogether.platform.school.academic.curriculum.ClassGrade;
import africa.growtogether.platform.school.academic.curriculum.ClassGradeRepository;
import africa.growtogether.platform.school.academic.curriculum.Stream;
import africa.growtogether.platform.school.academic.curriculum.StreamRepository;
import africa.growtogether.platform.school.academic.curriculum.SubjectOffering;
import africa.growtogether.platform.school.academic.curriculum.SubjectOfferingRepository;
import africa.growtogether.platform.school.academic.subject.Subject;
import africa.growtogether.platform.school.academic.subject.SubjectRepository;
import africa.growtogether.platform.school.timetable.bell.BellPeriod;
import africa.growtogether.platform.school.timetable.bell.BellPeriodRepository;
import africa.growtogether.platform.school.timetable.entry.TimetableEntry;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProgrammeLessonDetailProjectorTest {

    @Test
    void enrichesTenantScopedLessonAndSortsByPeriod() {

        BellPeriodRepository bellPeriods =
                mock(BellPeriodRepository.class);

        SubjectOfferingRepository subjectOfferings =
                mock(SubjectOfferingRepository.class);

        SubjectRepository subjects =
                mock(SubjectRepository.class);

        ClassGradeRepository classGrades =
                mock(ClassGradeRepository.class);

        StreamRepository streams =
                mock(StreamRepository.class);

        UUID tenantId = UUID.randomUUID();
        UUID timetableId = UUID.randomUUID();
        UUID bellPeriodId = UUID.randomUUID();
        UUID subjectOfferingId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();
        UUID streamId = UUID.randomUUID();

        TimetableEntry entry =
                mock(TimetableEntry.class);

        BellPeriod bellPeriod =
                mock(BellPeriod.class);

        SubjectOffering offering =
                mock(SubjectOffering.class);

        Subject subject =
                mock(Subject.class);

        ClassGrade classGrade =
                mock(ClassGrade.class);

        Stream stream =
                mock(Stream.class);

        when(entry.getTimetableId())
                .thenReturn(timetableId);

        when(entry.getBellPeriodId())
                .thenReturn(bellPeriodId);

        when(entry.getSubjectOfferingId())
                .thenReturn(subjectOfferingId);

        when(entry.getClassGradeId())
                .thenReturn(classGradeId);

        when(entry.getStreamId())
                .thenReturn(streamId);

        when(entry.getActivityName())
                .thenReturn("Mathematics");

        when(
                bellPeriods.findByTenantIdAndId(
                        tenantId,
                        bellPeriodId
                )
        ).thenReturn(
                Optional.of(bellPeriod)
        );

        when(bellPeriod.getPeriodCode())
                .thenReturn("P1");

        when(bellPeriod.getPeriodName())
                .thenReturn("Period 1");

        when(bellPeriod.getSequenceNumber())
                .thenReturn(1);

        when(bellPeriod.getStartTime())
                .thenReturn(LocalTime.of(8, 0));

        when(bellPeriod.getEndTime())
                .thenReturn(LocalTime.of(8, 40));

        when(
                subjectOfferings.findByTenantIdAndId(
                        tenantId,
                        subjectOfferingId
                )
        ).thenReturn(
                Optional.of(offering)
        );

        when(offering.getSubjectId())
                .thenReturn(subjectId);

        when(
                subjects.findByTenantIdAndId(
                        tenantId,
                        subjectId
                )
        ).thenReturn(
                Optional.of(subject)
        );

        when(subject.getSubjectCode())
                .thenReturn("MATH");

        when(subject.getSubjectName())
                .thenReturn("Mathematics");

        when(
                classGrades.findByTenantIdAndId(
                        tenantId,
                        classGradeId
                )
        ).thenReturn(
                Optional.of(classGrade)
        );

        when(classGrade.getClassCode())
                .thenReturn("P6");

        when(classGrade.getClassName())
                .thenReturn("Primary Six");

        when(
                streams.findByTenantIdAndId(
                        tenantId,
                        streamId
                )
        ).thenReturn(
                Optional.of(stream)
        );

        when(stream.getStreamCode())
                .thenReturn("A");

        when(stream.getStreamName())
                .thenReturn("Stream A");

        ProgrammeLessonDetailProjector projector =
                new ProgrammeLessonDetailProjector(
                        bellPeriods,
                        subjectOfferings,
                        subjects,
                        classGrades,
                        streams
                );

        List<ProgrammeLessonDetail> result =
                projector.project(
                        tenantId,
                        List.of(entry)
                );

        assertThat(result)
                .hasSize(1);

        ProgrammeLessonDetail detail =
                result.get(0);

        assertThat(detail.timetableId())
                .isEqualTo(timetableId);

        assertThat(detail.periodCode())
                .isEqualTo("P1");

        assertThat(detail.subjectCode())
                .isEqualTo("MATH");

        assertThat(detail.subjectName())
                .isEqualTo("Mathematics");

        assertThat(detail.classCode())
                .isEqualTo("P6");

        assertThat(detail.streamCode())
                .isEqualTo("A");

        assertThat(detail.startTime())
                .isEqualTo(LocalTime.of(8, 0));

        assertThat(detail.endTime())
                .isEqualTo(LocalTime.of(8, 40));
    }

    @Test
    void acceptsClassWideLessonWithoutStream() {

        BellPeriodRepository bellPeriods =
                mock(BellPeriodRepository.class);

        SubjectOfferingRepository subjectOfferings =
                mock(SubjectOfferingRepository.class);

        SubjectRepository subjects =
                mock(SubjectRepository.class);

        ClassGradeRepository classGrades =
                mock(ClassGradeRepository.class);

        StreamRepository streams =
                mock(StreamRepository.class);

        UUID tenantId = UUID.randomUUID();
        UUID bellPeriodId = UUID.randomUUID();
        UUID subjectOfferingId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();

        TimetableEntry entry =
                mock(TimetableEntry.class);

        BellPeriod bellPeriod =
                mock(BellPeriod.class);

        SubjectOffering offering =
                mock(SubjectOffering.class);

        Subject subject =
                mock(Subject.class);

        ClassGrade classGrade =
                mock(ClassGrade.class);

        when(entry.getBellPeriodId())
                .thenReturn(bellPeriodId);

        when(entry.getSubjectOfferingId())
                .thenReturn(subjectOfferingId);

        when(entry.getClassGradeId())
                .thenReturn(classGradeId);

        when(entry.getStreamId())
                .thenReturn(null);

        when(
                bellPeriods.findByTenantIdAndId(
                        tenantId,
                        bellPeriodId
                )
        ).thenReturn(
                Optional.of(bellPeriod)
        );

        when(
                subjectOfferings.findByTenantIdAndId(
                        tenantId,
                        subjectOfferingId
                )
        ).thenReturn(
                Optional.of(offering)
        );

        when(offering.getSubjectId())
                .thenReturn(subjectId);

        when(
                subjects.findByTenantIdAndId(
                        tenantId,
                        subjectId
                )
        ).thenReturn(
                Optional.of(subject)
        );

        when(
                classGrades.findByTenantIdAndId(
                        tenantId,
                        classGradeId
                )
        ).thenReturn(
                Optional.of(classGrade)
        );

        ProgrammeLessonDetailProjector projector =
                new ProgrammeLessonDetailProjector(
                        bellPeriods,
                        subjectOfferings,
                        subjects,
                        classGrades,
                        streams
                );

        ProgrammeLessonDetail detail =
                projector
                        .project(
                                tenantId,
                                List.of(entry)
                        )
                        .get(0);

        assertThat(detail.streamId())
                .isNull();

        assertThat(detail.streamCode())
                .isNull();

        assertThat(detail.streamName())
                .isNull();
    }
}
