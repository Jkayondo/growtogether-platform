package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.school.guardian.Guardian;
import africa.growtogether.platform.school.guardian.GuardianRepository;
import africa.growtogether.platform.school.relationship.StudentGuardianRelationship;
import africa.growtogether.platform.school.relationship.StudentGuardianRelationshipRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ConnectParentRelationshipAuthorizationService {

    private final EnterpriseIdentityContext identity;
    private final GuardianRepository guardians;
    private final StudentGuardianRelationshipRepository relationships;

    public ConnectParentRelationshipAuthorizationService(
            EnterpriseIdentityContext identity,
            GuardianRepository guardians,
            StudentGuardianRelationshipRepository relationships
    ) {
        this.identity = identity;
        this.guardians = guardians;
        this.relationships = relationships;
    }

    @Transactional(readOnly = true)
    public StudentGuardianRelationship requireCanCommunicateWithStudent(
            UUID studentId
    ) {
        UUID authenticatedUserId =
                identity.requireUserId();

        return requireUserCanCommunicateWithStudent(
                authenticatedUserId,
                studentId
        );
    }

    @Transactional(readOnly = true)
    public StudentGuardianRelationship requireUserCanCommunicateWithStudent(
            UUID userId,
            UUID studentId
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "userId must not be null"
            );
        }

        if (studentId == null) {
            throw new IllegalArgumentException(
                    "studentId must not be null"
            );
        }

        UUID tenantId =
                identity.requireTenantId();

        List<Guardian> linkedGuardians =
                guardians.findAllByTenantIdAndEiamUserId(
                        tenantId,
                        userId
                );

        if (linkedGuardians.isEmpty()) {
            throw denied();
        }

        LocalDate today =
                LocalDate.now();

        return linkedGuardians
                .stream()
                .flatMap(
                        guardian ->
                                relationships
                                        .findByTenantIdAndGuardianId(
                                                tenantId,
                                                guardian.getId()
                                        )
                                        .stream()
                )
                .filter(
                        relationship ->
                                studentId.equals(
                                        relationship.getStudentId()
                                )
                )
                .filter(
                        relationship ->
                                "ACTIVE".equals(
                                        relationship.getRelationshipStatus()
                                )
                )
                .filter(
                        relationship ->
                                relationship.getStatus()
                                        == EntityStatus.ACTIVE
                )
                .filter(
                        StudentGuardianRelationship::isReceivesCommunications
                )
                .filter(
                        relationship ->
                                !relationship
                                        .getEffectiveFrom()
                                        .isAfter(today)
                )
                .filter(
                        relationship ->
                                relationship.getEffectiveTo() == null
                                        || !relationship
                                                .getEffectiveTo()
                                                .isBefore(today)
                )
                .findFirst()
                .orElseThrow(
                        ConnectParentRelationshipAuthorizationService::denied
                );
    }

    private static AccessDeniedException denied() {
        return new AccessDeniedException(
                "Parent or guardian is not authorised to communicate for this learner"
        );
    }
}
