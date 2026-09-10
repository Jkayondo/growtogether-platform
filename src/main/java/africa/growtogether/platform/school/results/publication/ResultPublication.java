package africa.growtogether.platform.school.results.publication;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name="gts_result_publication")
public class ResultPublication extends AuditedTenantEntity {


    @Column(name="academic_year_id", nullable=false)
    private UUID academicYearId;


    @Column(name="academic_term_id")
    private UUID academicTermId;


    @Column(name="class_grade_id")
    private UUID classGradeId;


    @Column(name="publication_type",
            nullable=false,
            length=40)
    private String publicationType;


    @Column(name="published_at")
    private Instant publishedAt;


    @Column(name="published_by")
    private UUID publishedBy;


    @Column(name="publication_status",
            nullable=false,
            length=30)
    private String publicationStatus;


    @Column(name="publication_message",
            length=1000)
    private String publicationMessage;



    protected ResultPublication(){
    }



    public ResultPublication(
            UUID academicYearId,
            UUID academicTermId,
            UUID classGradeId,
            String publicationType
    ){

        this.academicYearId = academicYearId;
        this.academicTermId = academicTermId;
        this.classGradeId = classGradeId;
        this.publicationType = publicationType;
        this.publicationStatus = "DRAFT";

    }



    public void publish(
            UUID userId
    ){

        this.publishedBy = userId;
        this.publishedAt = Instant.now();
        this.publicationStatus = "PUBLISHED";

    }



    public void unpublish(
            UUID userId
    ){

        this.publicationStatus = "UNPUBLISHED";

    }



    public String getPublicationStatus(){

        return publicationStatus;

    }


}
