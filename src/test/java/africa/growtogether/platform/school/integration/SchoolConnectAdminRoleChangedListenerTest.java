package africa.growtogether.platform.school.integration;

import africa.growtogether.platform.connect.ConnectSpace;
import africa.growtogether.platform.eiam.role.events.UserRolesChangedEvent;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolConnectAdminRoleChangedListenerTest {

    @Mock
    private SchoolConnectSpaceResolver spaces;

    @Mock
    private SchoolConnectAdminMembershipReconciliationService
            reconciliation;


    @Test
    void roleChangeReconcilesWhenSchoolInstitutionSpaceExists() {

        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ConnectSpace space =
                mock(
                        ConnectSpace.class
                );

        when(
                spaces.findInstitutionSpace(
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        space
                )
        );

        SchoolConnectAdminRoleChangedListener listener =
                new SchoolConnectAdminRoleChangedListener(
                        spaces,
                        reconciliation
                );

        listener.handle(
                new UserRolesChangedEvent(
                        UUID.randomUUID(),
                        tenantId,
                        userId,
                        Instant.now()
                )
        );

        verify(
                reconciliation
        ).reconcile(
                tenantId,
                userId,
                space
        );
    }


    @Test
    void roleChangeIsSafeNoOpBeforeSchoolOnboarding() {

        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(
                spaces.findInstitutionSpace(
                        tenantId
                )
        ).thenReturn(
                Optional.empty()
        );

        SchoolConnectAdminRoleChangedListener listener =
                new SchoolConnectAdminRoleChangedListener(
                        spaces,
                        reconciliation
                );

        listener.handle(
                new UserRolesChangedEvent(
                        UUID.randomUUID(),
                        tenantId,
                        userId,
                        Instant.now()
                )
        );

        verifyNoInteractions(
                reconciliation
        );
    }
}
