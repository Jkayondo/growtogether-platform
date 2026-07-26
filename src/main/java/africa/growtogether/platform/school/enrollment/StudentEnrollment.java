package africa.growtogether.platform.school.enrollment;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(name = "gts_student_enrollment")
public class StudentEnrollment extends AuditedTenantEntity {


    @Column(name = "student_id", nullable = false)
    private UUID studentId;


    @Column(name = "academic_year_id", nullable = false)
    private UUID academicYearId;


    @Column(name = "academic_term_id")
    private UUID academicTermId;


    @Column(name = "campus_id", nullable = false)
    private UUID campusId;


    @Column(name = "class_grade_id", nullable = false)
    private UUID classGradeId;


    @Column(name = "stream_id")
    private UUID streamId;


    @Column(name = "enrollment_number", nullable = false, length = 100)
    private String enrollmentNumber;


    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;


    @Column(name = "enrollment_status", nullable = false, length = 30)
    private String enrollmentStatus = "ACTIVE";


    protected StudentEnrollment() {
    }


    public UUID getStudentId() {
        return studentId;
    }


    public UUID getAcademicYearId() {
        return academicYearId;
    }


    public UUID getCampusId() {
        return campusId;
    }


    public UUID getClassGradeId() {
        return classGradeId;
    }


    public UUID getStreamId() {
        return streamId;
    }


    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }
}
