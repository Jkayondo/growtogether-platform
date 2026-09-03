package africa.growtogether.platform.school.assessment.examination.schedule;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;


@Entity
@Table(name = "gts_examination_schedule")
public class ExaminationSchedule extends AuditedTenantEntity {


    @Column(
            name = "schedule_reference",
            nullable = false,
            length = 100
    )
    private String scheduleReference;


    @Column(
            name = "examination_session_id",
            nullable = false
    )
    private UUID examinationSessionId;


    @Column(
            name = "assessment_paper_id",
            nullable = false
    )
    private UUID assessmentPaperId;


    @Column(
            name = "class_offering_id",
            nullable = false
    )
    private UUID classOfferingId;


    @Column(
            name = "stream_id"
    )
    private UUID streamId;


    @Column(
            name = "examination_date",
            nullable = false
    )
    private LocalDate examinationDate;


    @Column(
            name = "start_time",
            nullable = false
    )
    private LocalTime startTime;


    @Column(
            name = "end_time",
            nullable = false
    )
    private LocalTime endTime;


    @Column(
            name = "scheduling_resource_id"
    )
    private UUID schedulingResourceId;


    @Column(
            name = "expected_candidate_count",
            nullable = false
    )
    private Integer expectedCandidateCount = 0;


    @Column(
            name = "timetable_entry_id"
    )
    private UUID timetableEntryId;


    @Column(
            name = "special_instructions",
            length = 2000
    )
    private String specialInstructions;


    @Column(
            name = "schedule_status",
            nullable = false,
            length = 30
    )
    private String scheduleStatus;


    protected ExaminationSchedule() {
    }


    public ExaminationSchedule(
            String scheduleReference,
            UUID examinationSessionId,
            UUID assessmentPaperId,
            UUID classOfferingId,
            LocalDate examinationDate,
            LocalTime startTime,
            LocalTime endTime
    ) {

        this.scheduleReference = scheduleReference;
        this.examinationSessionId = examinationSessionId;
        this.assessmentPaperId = assessmentPaperId;
        this.classOfferingId = classOfferingId;
        this.examinationDate = examinationDate;
        this.startTime = startTime;
        this.endTime = endTime;

        this.scheduleStatus = "SCHEDULED";
    }


    public String getScheduleReference() {
        return scheduleReference;
    }


    public UUID getExaminationSessionId() {
        return examinationSessionId;
    }


    public UUID getAssessmentPaperId() {
        return assessmentPaperId;
    }


    public UUID getClassOfferingId() {
        return classOfferingId;
    }


    public UUID getStreamId() {
        return streamId;
    }


    public LocalDate getExaminationDate() {
        return examinationDate;
    }


    public LocalTime getStartTime() {
        return startTime;
    }


    public LocalTime getEndTime() {
        return endTime;
    }


    public String getScheduleStatus() {
        return scheduleStatus;
    }


    public void confirm() {

        this.scheduleStatus = "CONFIRMED";

    }


    public void start() {

        this.scheduleStatus = "IN_PROGRESS";

    }


    public void complete() {

        this.scheduleStatus = "COMPLETED";

    }


    public void cancel() {

        this.scheduleStatus = "CANCELLED";

    }

}
