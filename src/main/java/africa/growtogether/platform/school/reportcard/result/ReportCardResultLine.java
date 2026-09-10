package africa.growtogether.platform.school.reportcard.result;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(name = "report_card_result_lines")
public class ReportCardResultLine extends AuditedTenantEntity {


    @Column(
            name="report_card_id",
            nullable=false
    )
    private UUID reportCardId;


    @Column(
            name="student_subject_result_id",
            nullable=false
    )
    private UUID studentSubjectResultId;


    @Column(
            name="subject_name",
            nullable=false,
            length=200
    )
    private String subjectName;


    @Column(name="final_score")
    private Double finalScore;


    @Column(
            name="grade_code",
            length=40
    )
    private String gradeCode;


    @Column(
            name="grade_name",
            length=120
    )
    private String gradeName;


    @Column(name="grade_point")
    private Double gradePoint;


    @Column(
            name="teacher_comment",
            length=2000
    )
    private String teacherComment;


    @Column(
            name="sequence_number",
            nullable=false
    )
    private Integer sequenceNumber;


    protected ReportCardResultLine(){
    }


    public ReportCardResultLine(
            UUID reportCardId,
            UUID studentSubjectResultId,
            String subjectName,
            Double finalScore,
            String gradeCode,
            String gradeName,
            Double gradePoint,
            Integer sequenceNumber
    ){

        this.reportCardId = reportCardId;
        this.studentSubjectResultId = studentSubjectResultId;
        this.subjectName = subjectName;
        this.finalScore = finalScore;
        this.gradeCode = gradeCode;
        this.gradeName = gradeName;
        this.gradePoint = gradePoint;
        this.sequenceNumber = sequenceNumber;

    }


    public UUID getReportCardId(){
        return reportCardId;
    }


    public UUID getStudentSubjectResultId(){
        return studentSubjectResultId;
    }


    public String getSubjectName(){
        return subjectName;
    }


    public Double getFinalScore(){
        return finalScore;
    }


    public String getGradeCode(){
        return gradeCode;
    }


    public String getGradeName(){
        return gradeName;
    }


    public Double getGradePoint(){
        return gradePoint;
    }

}
