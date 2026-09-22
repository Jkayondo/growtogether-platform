package africa.growtogether.platform.school.finance.statement.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import africa.growtogether.platform.eds.DocumentReference;
import africa.growtogether.platform.eds.DocumentReferenceService;

import africa.growtogether.platform.ens.NotificationChannel;
import africa.growtogether.platform.ens.NotificationDtos.SendCommand;
import africa.growtogether.platform.ens.NotificationDtos.View;
import africa.growtogether.platform.ens.NotificationService;

import africa.growtogether.platform.school.guardian.Guardian;
import africa.growtogether.platform.school.guardian.GuardianRepository;

import africa.growtogether.platform.school.relationship.StudentGuardianRelationship;
import africa.growtogether.platform.school.relationship.StudentGuardianRelationshipRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class FinanceStudentAccountStatementDeliveryServiceTest {

    @Test
    void emailDeliveryResolvesVerifiedGuardianServerSideAndUsesTenantBoundEns() {

        Fixture fixture =
                fixture();

        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        UUID statementReferenceId =
                UUID.randomUUID();

        UUID documentId =
                UUID.randomUUID();

        UUID notificationId =
                UUID.randomUUID();

        authorizeGuardian(
                fixture,
                tenantId,
                studentId,
                guardianId,
                "guardian@example.test",
                "+256700000001"
        );

        linkStatement(
                fixture,
                studentId,
                statementReferenceId,
                documentId
        );

        String expectedSource =
                "financial-statement:"
                + statementReferenceId;

        when(
                fixture.notifications.sendForTenant(
                        eq(tenantId),
                        any(SendCommand.class)
                )
        ).thenReturn(
                view(
                        notificationId,
                        tenantId,
                        "guardian@example.test",
                        NotificationChannel.EMAIL,
                        expectedSource
                )
        );

        var result =
                fixture.service.deliver(
                        tenantId,
                        studentId,
                        statementReferenceId,
                        new FinanceStudentAccountStatementDocumentDtos
                                .StatementDeliveryRequest(
                                        guardianId,
                                        NotificationChannel.EMAIL
                                )
                );

        assertThat(result.statementReferenceId())
                .isEqualTo(
                        statementReferenceId
                );

        assertThat(result.studentId())
                .isEqualTo(
                        studentId
                );

        assertThat(result.guardianId())
                .isEqualTo(
                        guardianId
                );

        assertThat(result.notificationId())
                .isEqualTo(
                        notificationId
                );

        assertThat(result.channel())
                .isEqualTo(
                        NotificationChannel.EMAIL
                );

        assertThat(result.deliveryRequestStatus())
                .isEqualTo(
                        "ENS_REQUEST_ACCEPTED"
                );

        ArgumentCaptor<SendCommand> command =
                ArgumentCaptor.forClass(
                        SendCommand.class
                );

        verify(fixture.notifications)
                .sendForTenant(
                        eq(tenantId),
                        command.capture()
                );

        assertThat(
                command.getValue().recipient()
        )
                .isEqualTo(
                        "guardian@example.test"
                );

        assertThat(
                command.getValue().channel()
        )
                .isEqualTo(
                        NotificationChannel.EMAIL
                );

        assertThat(
                command.getValue().sourceReference()
        )
                .isEqualTo(
                        expectedSource
                );

        assertThat(
                command.getValue().body()
        )
                .contains(
                        statementReferenceId.toString()
                )
                .doesNotContain(
                        "storageKey",
                        "statements/"
                );

        verify(fixture.identity)
                .requireTenant(
                        tenantId
                );
    }

    @Test
    void smsDeliveryUsesGuardianPhoneResolvedServerSide() {

        Fixture fixture =
                fixture();

        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        UUID statementReferenceId =
                UUID.randomUUID();

        UUID documentId =
                UUID.randomUUID();

        authorizeGuardian(
                fixture,
                tenantId,
                studentId,
                guardianId,
                "guardian@example.test",
                "+256700000002"
        );

        linkStatement(
                fixture,
                studentId,
                statementReferenceId,
                documentId
        );

        when(
                fixture.notifications.sendForTenant(
                        eq(tenantId),
                        any(SendCommand.class)
                )
        ).thenReturn(
                view(
                        UUID.randomUUID(),
                        tenantId,
                        "+256700000002",
                        NotificationChannel.SMS,
                        "financial-statement:"
                                + statementReferenceId
                )
        );

        fixture.service.deliver(
                tenantId,
                studentId,
                statementReferenceId,
                new FinanceStudentAccountStatementDocumentDtos
                        .StatementDeliveryRequest(
                                guardianId,
                                NotificationChannel.SMS
                        )
        );

        ArgumentCaptor<SendCommand> command =
                ArgumentCaptor.forClass(
                        SendCommand.class
                );

        verify(fixture.notifications)
                .sendForTenant(
                        eq(tenantId),
                        command.capture()
                );

        assertThat(
                command.getValue().recipient()
        )
                .isEqualTo(
                        "+256700000002"
                );

        assertThat(
                command.getValue().channel()
        )
                .isEqualTo(
                        NotificationChannel.SMS
                );
    }

    @Test
    void tenantMismatchFailsBeforeRelationshipDocumentOrEnsAccess() {

        Fixture fixture =
                fixture();

        UUID tenantId =
                UUID.randomUUID();

        doThrow(
                new SecurityException(
                        "Tenant access denied"
                )
        )
                .when(fixture.identity)
                .requireTenant(
                        tenantId
                );

        assertThatThrownBy(
                () ->
                        fixture.service.deliver(
                                tenantId,
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                new FinanceStudentAccountStatementDocumentDtos
                                        .StatementDeliveryRequest(
                                                UUID.randomUUID(),
                                                NotificationChannel.EMAIL
                                        )
                        )
        )
                .isInstanceOf(
                        SecurityException.class
                );

        verifyNoInteractions(
                fixture.relationships,
                fixture.guardians,
                fixture.references,
                fixture.notifications
        );
    }

    @Test
    void guardianWithoutStudentRelationshipIsRejectedBeforeEns() {

        Fixture fixture =
                fixture();

        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        when(
                fixture.relationships
                        .findByTenantIdAndStudentId(
                                tenantId,
                                studentId
                        )
        ).thenReturn(
                List.of()
        );

        assertThatThrownBy(
                () ->
                        fixture.service.deliver(
                                tenantId,
                                studentId,
                                UUID.randomUUID(),
                                new FinanceStudentAccountStatementDocumentDtos
                                        .StatementDeliveryRequest(
                                                UUID.randomUUID(),
                                                NotificationChannel.EMAIL
                                        )
                        )
        )
                .isInstanceOf(
                        SecurityException.class
                )
                .hasMessageContaining(
                        "not authorised"
                );

        verifyNoInteractions(
                fixture.guardians,
                fixture.references,
                fixture.notifications
        );
    }

    @Test
    void relationshipWithoutCommunicationConsentIsRejected() {

        Fixture fixture =
                fixture();

        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        StudentGuardianRelationship relationship =
                relationship(
                        studentId,
                        guardianId,
                        false
                );

        when(
                fixture.relationships
                        .findByTenantIdAndStudentId(
                                tenantId,
                                studentId
                        )
        ).thenReturn(
                List.of(
                        relationship
                )
        );

        assertThatThrownBy(
                () ->
                        fixture.service.deliver(
                                tenantId,
                                studentId,
                                UUID.randomUUID(),
                                new FinanceStudentAccountStatementDocumentDtos
                                        .StatementDeliveryRequest(
                                                guardianId,
                                                NotificationChannel.EMAIL
                                        )
                        )
        )
                .isInstanceOf(
                        SecurityException.class
                );

        verifyNoInteractions(
                fixture.guardians,
                fixture.references,
                fixture.notifications
        );
    }

    @Test
    void unverifiedGuardianIsRejectedBeforeStatementLookupAndEns() {

        Fixture fixture =
                fixture();

        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        StudentGuardianRelationship relationship =
                relationship(
                        studentId,
                        guardianId,
                        true
                );

        when(
                fixture.relationships
                        .findByTenantIdAndStudentId(
                                tenantId,
                                studentId
                        )
        ).thenReturn(
                List.of(
                        relationship
                )
        );

        Guardian guardian =
                mock(
                        Guardian.class
                );

        when(guardian.getStatus())
                .thenReturn(
                        EntityStatus.ACTIVE
                );

        when(guardian.getGuardianStatus())
                .thenReturn(
                        "ACTIVE"
                );

        when(guardian.getVerificationStatus())
                .thenReturn(
                        "UNVERIFIED"
                );

        when(
                fixture.guardians.findByTenantIdAndId(
                        tenantId,
                        guardianId
                )
        ).thenReturn(
                Optional.of(
                        guardian
                )
        );

        assertThatThrownBy(
                () ->
                        fixture.service.deliver(
                                tenantId,
                                studentId,
                                UUID.randomUUID(),
                                new FinanceStudentAccountStatementDocumentDtos
                                        .StatementDeliveryRequest(
                                                guardianId,
                                                NotificationChannel.EMAIL
                                        )
                        )
        )
                .isInstanceOf(
                        SecurityException.class
                )
                .hasMessageContaining(
                        "active and verified"
                );

        verifyNoInteractions(
                fixture.references,
                fixture.notifications
        );
    }

    @Test
    void statementBelongingToDifferentStudentIsRejectedBeforeEns() {

        Fixture fixture =
                fixture();

        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        UUID statementReferenceId =
                UUID.randomUUID();

        UUID statementDocumentId =
                UUID.randomUUID();

        authorizeGuardian(
                fixture,
                tenantId,
                studentId,
                guardianId,
                "guardian@example.test",
                "+256700000003"
        );

        DocumentReference statement =
                reference(
                        statementDocumentId
                );

        DocumentReference otherStudent =
                reference(
                        UUID.randomUUID()
                );

        when(
                fixture.references.findByReference(
                        "FINANCE_STATEMENT",
                        statementReferenceId
                )
        ).thenReturn(
                List.of(
                        statement
                )
        );

        when(
                fixture.references.findByReference(
                        "FINANCE_STATEMENT_STUDENT",
                        studentId
                )
        ).thenReturn(
                List.of(
                        otherStudent
                )
        );

        assertThatThrownBy(
                () ->
                        fixture.service.deliver(
                                tenantId,
                                studentId,
                                statementReferenceId,
                                new FinanceStudentAccountStatementDocumentDtos
                                        .StatementDeliveryRequest(
                                                guardianId,
                                                NotificationChannel.EMAIL
                                        )
                        )
        )
                .isInstanceOf(
                        SecurityException.class
                )
                .hasMessageContaining(
                        "does not belong"
                );

        verify(
                fixture.notifications,
                never()
        )
                .sendForTenant(
                        any(UUID.class),
                        any(SendCommand.class)
                );
    }

    @Test
    void ensTenantMismatchFailsClosed() {

        Fixture fixture =
                fixture();

        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        UUID statementReferenceId =
                UUID.randomUUID();

        authorizeGuardian(
                fixture,
                tenantId,
                studentId,
                guardianId,
                "guardian@example.test",
                "+256700000004"
        );

        linkStatement(
                fixture,
                studentId,
                statementReferenceId,
                UUID.randomUUID()
        );

        when(
                fixture.notifications.sendForTenant(
                        eq(tenantId),
                        any(SendCommand.class)
                )
        ).thenReturn(
                view(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "guardian@example.test",
                        NotificationChannel.EMAIL,
                        "financial-statement:"
                                + statementReferenceId
                )
        );

        assertThatThrownBy(
                () ->
                        fixture.service.deliver(
                                tenantId,
                                studentId,
                                statementReferenceId,
                                new FinanceStudentAccountStatementDocumentDtos
                                        .StatementDeliveryRequest(
                                                guardianId,
                                                NotificationChannel.EMAIL
                                        )
                        )
        )
                .isInstanceOf(
                        SecurityException.class
                )
                .hasMessageContaining(
                        "tenant mismatch"
                );
    }

    private static void authorizeGuardian(
            Fixture fixture,
            UUID tenantId,
            UUID studentId,
            UUID guardianId,
            String email,
            String phone
    ) {

        StudentGuardianRelationship relationship =
                relationship(
                        studentId,
                        guardianId,
                        true
                );

        when(
                fixture.relationships
                        .findByTenantIdAndStudentId(
                                tenantId,
                                studentId
                        )
        ).thenReturn(
                List.of(
                        relationship
                )
        );

        Guardian guardian =
                mock(
                        Guardian.class
                );

        when(guardian.getStatus())
                .thenReturn(
                        EntityStatus.ACTIVE
                );

        when(guardian.getGuardianStatus())
                .thenReturn(
                        "ACTIVE"
                );

        when(guardian.getVerificationStatus())
                .thenReturn(
                        "VERIFIED"
                );

        when(guardian.getEmail())
                .thenReturn(
                        email
                );

        when(guardian.getPrimaryPhoneNumber())
                .thenReturn(
                        phone
                );

        when(
                fixture.guardians.findByTenantIdAndId(
                        tenantId,
                        guardianId
                )
        ).thenReturn(
                Optional.of(
                        guardian
                )
        );
    }

    private static StudentGuardianRelationship relationship(
            UUID studentId,
            UUID guardianId,
            boolean receivesCommunications
    ) {

        StudentGuardianRelationship relationship =
                mock(
                        StudentGuardianRelationship.class
                );

        when(relationship.getStudentId())
                .thenReturn(
                        studentId
                );

        when(relationship.getGuardianId())
                .thenReturn(
                        guardianId
                );

        when(relationship.getStatus())
                .thenReturn(
                        EntityStatus.ACTIVE
                );

        when(relationship.getRelationshipStatus())
                .thenReturn(
                        "ACTIVE"
                );

        when(relationship.isReceivesCommunications())
                .thenReturn(
                        receivesCommunications
                );

        return relationship;
    }

    private static void linkStatement(
            Fixture fixture,
            UUID studentId,
            UUID statementReferenceId,
            UUID documentId
    ) {

        DocumentReference linkedReference =
                reference(
                        documentId
                );

        when(
                fixture.references.findByReference(
                        "FINANCE_STATEMENT",
                        statementReferenceId
                )
        ).thenReturn(
                List.of(
                        linkedReference
                )
        );

        when(
                fixture.references.findByReference(
                        "FINANCE_STATEMENT_STUDENT",
                        studentId
                )
        ).thenReturn(
                List.of(
                        linkedReference
                )
        );
    }

    private static DocumentReference reference(
            UUID documentId
    ) {

        DocumentReference reference =
                mock(
                        DocumentReference.class
                );

        when(reference.getDocumentId())
                .thenReturn(
                        documentId
                );

        return reference;
    }

    private static View view(
            UUID notificationId,
            UUID tenantId,
            String recipient,
            NotificationChannel channel,
            String sourceReference
    ) {

        return new View(
                notificationId,
                tenantId,
                "GT_SCHOOL_FINANCIAL_STATEMENT",
                recipient,
                channel,
                null,
                null,
                "GT School learner financial statement",
                "gt-school-finance",
                sourceReference,
                0,
                null,
                null,
                null
        );
    }

    private static Fixture fixture() {

        EnterpriseIdentityContext identity =
                mock(
                        EnterpriseIdentityContext.class
                );

        DocumentReferenceService references =
                mock(
                        DocumentReferenceService.class
                );

        StudentGuardianRelationshipRepository relationships =
                mock(
                        StudentGuardianRelationshipRepository.class
                );

        GuardianRepository guardians =
                mock(
                        GuardianRepository.class
                );

        NotificationService notifications =
                mock(
                        NotificationService.class
                );

        FinanceStudentAccountStatementDeliveryService service =
                new FinanceStudentAccountStatementDeliveryService(
                        identity,
                        references,
                        relationships,
                        guardians,
                        notifications
                );

        return new Fixture(
                identity,
                references,
                relationships,
                guardians,
                notifications,
                service
        );
    }

    private record Fixture(
            EnterpriseIdentityContext identity,
            DocumentReferenceService references,
            StudentGuardianRelationshipRepository relationships,
            GuardianRepository guardians,
            NotificationService notifications,
            FinanceStudentAccountStatementDeliveryService service
    ) {
    }
}
