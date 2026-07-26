package africa.growtogether.platform.school.attendance;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;


@Entity
@Table(name = "gts_student_attendance")
public class StudentAttendance extends AuditedTenantEntity {


    @Column(name = "attendance_session_id", nullable = false)
    private UUID attendanceSessionId;


    @Column(name = "student_id", nullable = false)
    private UUID studentId;


    @Column(name = "student_enrollment_id", nullable = false)
    private UUID studentEnrollmentId;


    @Column(
        name = "attendance_status",
        nullable = false,
        length = 30
    )
    private String attendanceStatus;


    @Column(name = "scheduled_arrival_time")
    private LocalTime scheduledArrivalTime;


    @Column(name = "actual_arrival_time")
    private LocalTime actualArrivalTime;


    @Column(name = "minutes_late", nullable = false)
    private Integer minutesLate = 0;


    @Column(name = "minutes_early_departure", nullable = false)
    private Integer minutesEarlyDeparture = 0;


    @Column(name = "attendance_reason_id")
    private UUID attendanceReasonId;


    @Column(name = "reason_notes", length = 1000)
    private String reasonNotes;


    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;


    @Column(name = "verified", nullable = false)
    private Boolean verified = false;


    @Column(
        name = "notification_status",
        nullable = false,
        length = 30
    )
    private String notificationStatus;


    @Column(
        name = "record_status",
        nullable = false,
        length = 30
    )
    private String recordStatus;


    protected StudentAttendance() {
    }


    public UUID getStudentId() {
        return studentId;
    }


    public UUID getStudentEnrollmentId() {
        return studentEnrollmentId;
    }


    public String getAttendanceStatus() {
        return attendanceStatus;
    }


    public Integer getMinutesLate() {
        return minutesLate;
    }


    public Integer getMinutesEarlyDeparture() {
        return minutesEarlyDeparture;
    }


    public Boolean isVerified() {
        return verified;
    }


    public String getRecordStatus() {
        return recordStatus;
    }
}
