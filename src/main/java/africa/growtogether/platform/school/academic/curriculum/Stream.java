package africa.growtogether.platform.school.academic.curriculum;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import africa.growtogether.platform.common.persistence.EntityStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "gts_stream")
public class Stream extends AuditedTenantEntity {

    @Column(
            name = "campus_id",
            nullable = false
    )
    private UUID campusId;

    @Column(
            name = "class_grade_id",
            nullable = false
    )
    private UUID classGradeId;

    @Column(
            name = "stream_code",
            nullable = false,
            length = 60
    )
    private String streamCode;

    @Column(
            name = "stream_name",
            nullable = false,
            length = 160
    )
    private String streamName;

    @Column(name = "capacity")
    private Integer capacity;

    protected Stream() {
    }

    public Stream(
            UUID campusId,
            UUID classGradeId,
            String streamCode,
            String streamName,
            Integer capacity
    ) {
        this.campusId = campusId;
        this.classGradeId = classGradeId;
        this.streamCode = streamCode;
        this.streamName = streamName;
        this.capacity = capacity;
    }

    public UUID getCampusId() {
        return campusId;
    }

    public UUID getClassGradeId() {
        return classGradeId;
    }

    public String getStreamCode() {
        return streamCode;
    }

    public String getStreamName() {
        return streamName;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void activate() {
        setStatus(EntityStatus.ACTIVE);
    }

    public void deactivate() {
        setStatus(EntityStatus.INACTIVE);
    }
}
