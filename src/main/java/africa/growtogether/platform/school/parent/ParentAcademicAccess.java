package africa.growtogether.platform.school.parent;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(
        name = "parent_academic_access",
        indexes = {
                @Index(
                        name = "ix_parent_academic_access_tenant",
                        columnList = "tenant_id"
                )
        }
)
public class ParentAcademicAccess
        extends AuditedTenantEntity {


    @Column(
            name = "parent_id",
            nullable = false
    )
    private UUID parentId;


    @Column(
            name = "learner_id",
            nullable = false
    )
    private UUID learnerId;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "access_status",
            nullable = false,
            length = 30
    )
    private ParentAcademicAccessStatus accessStatus;


    protected ParentAcademicAccess() {
    }


    public ParentAcademicAccess(
            UUID tenantId,
            UUID parentId,
            UUID learnerId
    ) {

        setTenantId(tenantId);

        this.parentId = parentId;
        this.learnerId = learnerId;
        this.accessStatus =
                ParentAcademicAccessStatus.ACTIVE;
    }


    public UUID getParentId() {
        return parentId;
    }


    public UUID getLearnerId() {
        return learnerId;
    }


    public ParentAcademicAccessStatus getAccessStatus() {
        return accessStatus;
    }


    public void suspend() {

        this.accessStatus =
                ParentAcademicAccessStatus.SUSPENDED;
    }


    public void revoke() {

        this.accessStatus =
                ParentAcademicAccessStatus.REVOKED;
    }
}
