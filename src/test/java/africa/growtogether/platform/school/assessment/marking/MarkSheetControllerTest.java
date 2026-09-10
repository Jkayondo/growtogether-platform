package africa.growtogether.platform.school.assessment.marking;


import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;


class MarkSheetControllerTest {


    private MarkSheetService service;

    private EnterpriseIdentityContext identity;

    private MarkSheetController controller;

    private UUID tenantId;

    private UUID actorId;


    @BeforeEach
    void setUp() {

        service =
                mock(
                        MarkSheetService.class
                );

        identity =
                mock(
                        EnterpriseIdentityContext.class
                );

        controller =
                new MarkSheetController(
                        service,
                        identity
                );

        tenantId =
                UUID.randomUUID();

        actorId =
                UUID.randomUUID();


        when(
                identity.requireTenantId()
        )
                .thenReturn(
                        tenantId
                );

        when(
                identity.requireUserId()
        )
                .thenReturn(
                        actorId
                );
    }


    @Test
    void createDerivesTenantFromIdentity() {

        CreateMarkSheetCommand command =
                new CreateMarkSheetCommand(
                        "MS-CTRL-001",
                        UUID.randomUUID(),
                        null,
                        null,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        null,
                        new BigDecimal("100.00"),
                        null
                );


        MarkSheet sheet =
                mock(
                        MarkSheet.class
                );


        when(
                service.create(
                        tenantId,
                        command
                )
        )
                .thenReturn(
                        sheet
                );


        assertSame(
                sheet,
                controller.create(
                        command
                )
        );


        verify(
                service
        )
                .create(
                        tenantId,
                        command
                );
    }


    @Test
    void getDerivesTenantFromIdentity() {

        UUID markSheetId =
                UUID.randomUUID();

        MarkSheet sheet =
                mock(
                        MarkSheet.class
                );


        when(
                service.get(
                        tenantId,
                        markSheetId
                )
        )
                .thenReturn(
                        sheet
                );


        assertSame(
                sheet,
                controller.get(
                        markSheetId
                )
        );


        verify(
                service
        )
                .get(
                        tenantId,
                        markSheetId
                );
    }


    @Test
    void openDerivesTenantAndActorFromIdentity() {

        UUID markSheetId =
                UUID.randomUUID();

        MarkSheet sheet =
                mock(
                        MarkSheet.class
                );


        when(
                service.open(
                        tenantId,
                        markSheetId,
                        actorId
                )
        )
                .thenReturn(
                        sheet
                );


        assertSame(
                sheet,
                controller.open(
                        markSheetId
                )
        );


        verify(
                service
        )
                .open(
                        tenantId,
                        markSheetId,
                        actorId
                );
    }


    @Test
    void submitDerivesTenantAndActorFromIdentity() {

        UUID markSheetId =
                UUID.randomUUID();

        MarkSheet sheet =
                mock(
                        MarkSheet.class
                );


        when(
                service.submit(
                        tenantId,
                        markSheetId,
                        actorId
                )
        )
                .thenReturn(
                        sheet
                );


        assertSame(
                sheet,
                controller.submit(
                        markSheetId
                )
        );


        verify(
                service
        )
                .submit(
                        tenantId,
                        markSheetId,
                        actorId
                );
    }
}
