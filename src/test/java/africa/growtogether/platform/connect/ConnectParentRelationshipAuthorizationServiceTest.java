package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.school.guardian.Guardian;
import africa.growtogether.platform.school.guardian.GuardianRepository;
import africa.growtogether.platform.school.relationship.StudentGuardianRelationship;
import africa.growtogether.platform.school.relationship.StudentGuardianRelationshipRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectParentRelationshipAuthorizationServiceTest {

    @Mock
    private EnterpriseIdentityContext identity;

    @Mock
    private GuardianRepository guardians;

    @Mock
    private StudentGuardianRelationshipRepository relationships;

    private ConnectParentRelationshipAuthorizationService service;

    private UUID tenantId;
    private UUID authenticatedUserId;
    private UUID guardianId;
    private UUID studentId;

    @BeforeEach
    void setUp() {
        service =
                new ConnectParentRelationshipAuthorizationService(
                        identity,
                        guardians,
                        relationships
                );

        tenantId = UUID.randomUUID();
        authenticatedUserId = UUID.randomUUID();
        guardianId = UUID.randomUUID();
        studentId = UUID.randomUUID();
    }

    @Test
    void activeCommunicationRelationshipAllowsAccess() {

        stubIdentity();

        Guardian guardian =
                guardian();

        when(
                guardians.findAllByTenantIdAndEiamUserId(
                        tenantId,
                        authenticatedUserId
                )
        ).thenReturn(
                List.of(guardian)
        );

        StudentGuardianRelationship relationship =
                relationship(
                        studentId,
                        true
                );

        when(
                relationships.findByTenantIdAndGuardianId(
                        tenantId,
                        guardianId
                )
        ).thenReturn(
                List.of(relationship)
        );

        StudentGuardianRelationship authorised =
                service.requireCanCommunicateWithStudent(
                        studentId
                );

        assertSame(
                relationship,
                authorised
        );
    }

    @Test
    void guardianWithoutCommunicationConsentIsDenied() {

        stubIdentity();

        Guardian guardian =
                guardian();

        when(
                guardians.findAllByTenantIdAndEiamUserId(
                        tenantId,
                        authenticatedUserId
                )
        ).thenReturn(
                List.of(guardian)
        );

        when(
                relationships.findByTenantIdAndGuardianId(
                        tenantId,
                        guardianId
                )
        ).thenReturn(
                List.of(
                        relationship(
                                studentId,
                                false
                        )
                )
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.requireCanCommunicateWithStudent(
                                studentId
                        )
        );
    }

    @Test
    void restrictedRelationshipIsDenied() {

        stubIdentity();

        Guardian guardian =
                guardian();

        when(
                guardians.findAllByTenantIdAndEiamUserId(
                        tenantId,
                        authenticatedUserId
                )
        ).thenReturn(
                List.of(guardian)
        );

        StudentGuardianRelationship relationship =
                relationship(
                        studentId,
                        true
                );

        relationship.restrict();

        when(
                relationships.findByTenantIdAndGuardianId(
                        tenantId,
                        guardianId
                )
        ).thenReturn(
                List.of(relationship)
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.requireCanCommunicateWithStudent(
                                studentId
                        )
        );
    }

    @Test
    void authenticatedUserWithoutGuardianProfileIsDenied() {

        stubIdentity();

        when(
                guardians.findAllByTenantIdAndEiamUserId(
                        tenantId,
                        authenticatedUserId
                )
        ).thenReturn(
                List.of()
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.requireCanCommunicateWithStudent(
                                studentId
                        )
        );

        verifyNoInteractions(
                relationships
        );
    }

    @Test
    void relationshipToAnotherStudentDoesNotGrantAccess() {

        stubIdentity();

        Guardian guardian =
                guardian();

        when(
                guardians.findAllByTenantIdAndEiamUserId(
                        tenantId,
                        authenticatedUserId
                )
        ).thenReturn(
                List.of(guardian)
        );

        when(
                relationships.findByTenantIdAndGuardianId(
                        tenantId,
                        guardianId
                )
        ).thenReturn(
                List.of(
                        relationship(
                                UUID.randomUUID(),
                                true
                        )
                )
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.requireCanCommunicateWithStudent(
                                studentId
                        )
        );
    }

    @Test
    void explicitTargetUserAuthorizationUsesTargetUserIdentity() {

        UUID targetParentUserId =
                UUID.randomUUID();

        when(
                identity.requireTenantId()
        ).thenReturn(
                tenantId
        );

        Guardian guardian =
                guardian();

        when(
                guardians.findAllByTenantIdAndEiamUserId(
                        tenantId,
                        targetParentUserId
                )
        ).thenReturn(
                List.of(guardian)
        );

        StudentGuardianRelationship relationship =
                relationship(
                        studentId,
                        true
                );

        when(
                relationships.findByTenantIdAndGuardianId(
                        tenantId,
                        guardianId
                )
        ).thenReturn(
                List.of(relationship)
        );

        assertSame(
                relationship,
                service.requireUserCanCommunicateWithStudent(
                        targetParentUserId,
                        studentId
                )
        );

        verify(
                guardians
        ).findAllByTenantIdAndEiamUserId(
                tenantId,
                targetParentUserId
        );

        verify(
                identity,
                never()
        ).requireUserId();
    }

    private void stubIdentity() {

        when(
                identity.requireTenantId()
        ).thenReturn(
                tenantId
        );

        when(
                identity.requireUserId()
        ).thenReturn(
                authenticatedUserId
        );
    }

    private Guardian guardian() {

        Guardian guardian =
                mock(Guardian.class);

        when(
                guardian.getId()
        ).thenReturn(
                guardianId
        );

        return guardian;
    }

    private StudentGuardianRelationship relationship(
            UUID relationshipStudentId,
            boolean receivesCommunications
    ) {

        StudentGuardianRelationship relationship =
                new StudentGuardianRelationship(
                        relationshipStudentId,
                        guardianId,
                        "MOTHER",
                        null,
                        true,
                        true,
                        false,
                        true,
                        "JOINT",
                        null,
                        true,
                        true,
                        receivesCommunications,
                        true,
                        true,
                        false,
                        true
                );

        relationship.setTenantId(
                tenantId
        );

        return relationship;
    }
}
