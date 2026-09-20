package africa.growtogether.platform.school.finance.receipt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.ens.NotificationChannel;
import africa.growtogether.platform.ens.NotificationDtos.SendCommand;
import africa.growtogether.platform.ens.NotificationService;
import africa.growtogether.platform.school.finance.receipt.FinancePaymentReceiptDtos.DeliveryRequest;
import africa.growtogether.platform.school.finance.receipt.FinancePaymentReceiptDtos.DeliveryResponse;
import africa.growtogether.platform.school.finance.receipt.FinancePaymentReceiptDtos.ReceiptResponse;
import africa.growtogether.platform.school.guardian.Guardian;
import africa.growtogether.platform.school.guardian.GuardianRepository;
import africa.growtogether.platform.school.relationship.StudentGuardianRelationship;
import africa.growtogether.platform.school.relationship.StudentGuardianRelationshipRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class FinancePaymentReceiptAuthorizedRecipientSecurityTest {

    @Test
    void deliveryRequestExposesGuardianIdentityButNoRawRecipient() {

        List<String> components =
                Arrays.stream(
                                DeliveryRequest.class
                                        .getRecordComponents()
                        )
                        .map(
                                java.lang.reflect.RecordComponent::getName
                        )
                        .toList();

        assertEquals(
                List.of(
                        "guardianId",
                        "channel"
                ),
                components
        );

        assertFalse(
                components.contains(
                        "recipient"
                )
        );
    }

    @Test
    void deliversEmailOnlyToAuthorisedVerifiedGuardianEmail() {

        Fixture fixture =
                fixture(
                        NotificationChannel.EMAIL
                );

        when(
                fixture.guardian.getEmail()
        ).thenReturn(
                "parent@example.test"
        );

        DeliveryResponse result =
                fixture.service.deliver(
                        fixture.tenantId,
                        fixture.receiptId,
                        new DeliveryRequest(
                                fixture.guardianId,
                                NotificationChannel.EMAIL
                        )
                );

        ArgumentCaptor<SendCommand> command =
                ArgumentCaptor.forClass(
                        SendCommand.class
                );

        verify(
                fixture.notifications
        ).send(
                command.capture()
        );

        assertEquals(
                "parent@example.test",
                command.getValue().recipient()
        );

        assertEquals(
                NotificationChannel.EMAIL,
                command.getValue().channel()
        );

        assertEquals(
                "parent@example.test",
                result.recipient()
        );

        assertEquals(
                "ENS_REQUEST_ACCEPTED",
                result.deliveryRequestStatus()
        );
    }

    @Test
    void deliversSmsOnlyToAuthorisedVerifiedGuardianPrimaryPhone() {

        Fixture fixture =
                fixture(
                        NotificationChannel.SMS
                );

        when(
                fixture.guardian.getPrimaryPhoneNumber()
        ).thenReturn(
                "+256700000001"
        );

        DeliveryResponse result =
                fixture.service.deliver(
                        fixture.tenantId,
                        fixture.receiptId,
                        new DeliveryRequest(
                                fixture.guardianId,
                                NotificationChannel.SMS
                        )
                );

        ArgumentCaptor<SendCommand> command =
                ArgumentCaptor.forClass(
                        SendCommand.class
                );

        verify(
                fixture.notifications
        ).send(
                command.capture()
        );

        assertEquals(
                "+256700000001",
                command.getValue().recipient()
        );

        assertEquals(
                NotificationChannel.SMS,
                command.getValue().channel()
        );

        assertEquals(
                "+256700000001",
                result.recipient()
        );
    }

    @Test
    void rejectsGuardianWhoIsNotRelatedToReceiptStudent() {

        Fixture fixture =
                fixture(
                        NotificationChannel.EMAIL
                );

        when(
                fixture.relationship.getGuardianId()
        ).thenReturn(
                UUID.randomUUID()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                fixture.service.deliver(
                                        fixture.tenantId,
                                        fixture.receiptId,
                                        new DeliveryRequest(
                                                fixture.guardianId,
                                                NotificationChannel.EMAIL
                                        )
                                )
                );

        assertTrue(
                error.getMessage()
                        .contains(
                                "not authorised"
                        )
        );

        verify(
                fixture.guardians,
                never()
        ).findByTenantIdAndId(
                any(),
                any()
        );

        verify(
                fixture.notifications,
                never()
        ).send(
                any()
        );
    }

    @Test
    void rejectsInactiveRelationshipEntity() {

        Fixture fixture =
                fixture(
                        NotificationChannel.EMAIL
                );

        when(
                fixture.relationship.getStatus()
        ).thenReturn(
                EntityStatus.INACTIVE
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        fixture.service.deliver(
                                fixture.tenantId,
                                fixture.receiptId,
                                new DeliveryRequest(
                                        fixture.guardianId,
                                        NotificationChannel.EMAIL
                                )
                        )
        );

        verify(
                fixture.notifications,
                never()
        ).send(
                any()
        );
    }

    @Test
    void rejectsRestrictedRelationshipDomainStatus() {

        Fixture fixture =
                fixture(
                        NotificationChannel.EMAIL
                );

        when(
                fixture.relationship.getRelationshipStatus()
        ).thenReturn(
                "RESTRICTED"
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        fixture.service.deliver(
                                fixture.tenantId,
                                fixture.receiptId,
                                new DeliveryRequest(
                                        fixture.guardianId,
                                        NotificationChannel.EMAIL
                                )
                        )
        );

        verify(
                fixture.notifications,
                never()
        ).send(
                any()
        );
    }

    @Test
    void rejectsRelationshipWithoutCommunicationsAuthority() {

        Fixture fixture =
                fixture(
                        NotificationChannel.EMAIL
                );

        when(
                fixture.relationship.isReceivesCommunications()
        ).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        fixture.service.deliver(
                                fixture.tenantId,
                                fixture.receiptId,
                                new DeliveryRequest(
                                        fixture.guardianId,
                                        NotificationChannel.EMAIL
                                )
                        )
        );

        verify(
                fixture.notifications,
                never()
        ).send(
                any()
        );
    }

    @Test
    void rejectsInactiveGuardian() {

        Fixture fixture =
                fixture(
                        NotificationChannel.EMAIL
                );

        when(
                fixture.guardian.getStatus()
        ).thenReturn(
                EntityStatus.INACTIVE
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        fixture.service.deliver(
                                fixture.tenantId,
                                fixture.receiptId,
                                new DeliveryRequest(
                                        fixture.guardianId,
                                        NotificationChannel.EMAIL
                                )
                        )
        );

        verify(
                fixture.notifications,
                never()
        ).send(
                any()
        );
    }

    @Test
    void rejectsUnverifiedGuardian() {

        Fixture fixture =
                fixture(
                        NotificationChannel.EMAIL
                );

        when(
                fixture.guardian.getVerificationStatus()
        ).thenReturn(
                "PENDING"
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        fixture.service.deliver(
                                fixture.tenantId,
                                fixture.receiptId,
                                new DeliveryRequest(
                                        fixture.guardianId,
                                        NotificationChannel.EMAIL
                                )
                        )
        );

        verify(
                fixture.notifications,
                never()
        ).send(
                any()
        );
    }

    @Test
    void rejectsUnsupportedDeliveryChannelAtRequestBoundary() {

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new DeliveryRequest(
                                        UUID.randomUUID(),
                                        NotificationChannel.WHATSAPP
                                )
                );

        assertEquals(
                "Receipt delivery supports EMAIL or SMS only",
                error.getMessage()
        );
    }

    @Test
    void rejectsMissingGuardianOwnedDestination() {

        Fixture fixture =
                fixture(
                        NotificationChannel.EMAIL
                );

        when(
                fixture.guardian.getEmail()
        ).thenReturn(
                "   "
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                fixture.service.deliver(
                                        fixture.tenantId,
                                        fixture.receiptId,
                                        new DeliveryRequest(
                                                fixture.guardianId,
                                                NotificationChannel.EMAIL
                                        )
                                )
                );

        assertEquals(
                "Authorised guardian has no email address",
                error.getMessage()
        );

        verify(
                fixture.notifications,
                never()
        ).send(
                any()
        );
    }

    private static Fixture fixture(
            NotificationChannel channel
    ) {

        UUID tenantId =
                UUID.randomUUID();

        UUID receiptId =
                UUID.randomUUID();

        UUID paymentId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        UUID documentId =
                UUID.randomUUID();

        FinancePaymentReceiptService receipts =
                mock(
                        FinancePaymentReceiptService.class
                );

        NotificationService notifications =
                mock(
                        NotificationService.class
                );

        StudentGuardianRelationshipRepository relationships =
                mock(
                        StudentGuardianRelationshipRepository.class
                );

        GuardianRepository guardians =
                mock(
                        GuardianRepository.class
                );

        StudentGuardianRelationship relationship =
                mock(
                        StudentGuardianRelationship.class
                );

        Guardian guardian =
                mock(
                        Guardian.class
                );

        ReceiptResponse receipt =
                new ReceiptResponse(
                        receiptId,
                        paymentId,
                        studentId,
                        "RCT-0000000001",
                        Instant.parse(
                                "2026-09-20T12:00:00Z"
                        ),
                        "UGX",
                        new BigDecimal(
                                "150000"
                        ),
                        Instant.parse(
                                "2026-09-20T12:01:00Z"
                        ),
                        UUID.randomUUID(),
                        documentId,
                        null,
                        null,
                        "ISSUED",
                        "ACTIVE",
                        "PAY-001",
                        "CASH",
                        null,
                        null,
                        null
                );

        when(
                receipts.getById(
                        tenantId,
                        receiptId
                )
        ).thenReturn(
                receipt
        );

        when(
                relationships.findByTenantIdAndStudentId(
                        tenantId,
                        studentId
                )
        ).thenReturn(
                List.of(
                        relationship
                )
        );

        when(
                relationship.getGuardianId()
        ).thenReturn(
                guardianId
        );

        when(
                relationship.getStatus()
        ).thenReturn(
                EntityStatus.ACTIVE
        );

        when(
                relationship.getRelationshipStatus()
        ).thenReturn(
                "ACTIVE"
        );

        when(
                relationship.isReceivesCommunications()
        ).thenReturn(true);

        when(
                guardians.findByTenantIdAndId(
                        tenantId,
                        guardianId
                )
        ).thenReturn(
                Optional.of(
                        guardian
                )
        );

        when(
                guardian.getStatus()
        ).thenReturn(
                EntityStatus.ACTIVE
        );

        when(
                guardian.getGuardianStatus()
        ).thenReturn(
                "ACTIVE"
        );

        when(
                guardian.getVerificationStatus()
        ).thenReturn(
                "VERIFIED"
        );

        if (channel == NotificationChannel.EMAIL) {
            when(
                    guardian.getEmail()
            ).thenReturn(
                    "parent@example.test"
            );
        }

        if (channel == NotificationChannel.SMS) {
            when(
                    guardian.getPrimaryPhoneNumber()
            ).thenReturn(
                    "+256700000001"
            );
        }

        FinancePaymentReceiptDeliveryService service =
                new FinancePaymentReceiptDeliveryService(
                        receipts,
                        notifications,
                        relationships,
                        guardians
                );

        return new Fixture(
                tenantId,
                receiptId,
                guardianId,
                service,
                notifications,
                relationships,
                guardians,
                relationship,
                guardian
        );
    }

    private record Fixture(
            UUID tenantId,
            UUID receiptId,
            UUID guardianId,
            FinancePaymentReceiptDeliveryService service,
            NotificationService notifications,
            StudentGuardianRelationshipRepository relationships,
            GuardianRepository guardians,
            StudentGuardianRelationship relationship,
            Guardian guardian
    ) {
    }
}
