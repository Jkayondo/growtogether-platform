package africa.growtogether.platform.eaif.audit;

import africa.growtogether.platform.eaif.AiEnums;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EaifAuditServiceActorAttributionTest {

    private final EaifExecutionAuditRepository repository =
            mock(EaifExecutionAuditRepository.class);

    private final EaifAuditService service =
            new EaifAuditService(repository);

    @Test
    void attributesAuthenticatedExecutionActor() {

        UUID tenantId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();

        EaifExecutionAudit audit =
                audit(
                        tenantId,
                        requestId
                );

        when(
                repository.findByTenantIdAndAiRequestId(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                Optional.of(audit)
        );

        EaifExecutionAudit result =
                service.attributeActor(
                        tenantId,
                        requestId,
                        actorUserId
                );

        assertSame(
                audit,
                result
        );

        assertEquals(
                actorUserId,
                result.actorUserId()
        );
    }

    @Test
    void rejectsNullExecutionActor() {

        UUID tenantId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        EaifExecutionAudit audit =
                audit(
                        tenantId,
                        requestId
                );

        when(
                repository.findByTenantIdAndAiRequestId(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                Optional.of(audit)
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.attributeActor(
                                tenantId,
                                requestId,
                                null
                        )
        );

        assertNull(
                audit.actorUserId()
        );
    }

    @Test
    void refusesExecutionActorReplacement() {

        UUID tenantId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        UUID firstActor =
                UUID.randomUUID();

        UUID secondActor =
                UUID.randomUUID();

        EaifExecutionAudit audit =
                audit(
                        tenantId,
                        requestId
                );

        when(
                repository.findByTenantIdAndAiRequestId(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                Optional.of(audit)
        );

        service.attributeActor(
                tenantId,
                requestId,
                firstActor
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.attributeActor(
                                tenantId,
                                requestId,
                                secondActor
                        )
        );

        assertEquals(
                firstActor,
                audit.actorUserId()
        );
    }

    @Test
    void sameExecutionActorIsIdempotent() {

        UUID tenantId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();

        EaifExecutionAudit audit =
                audit(
                        tenantId,
                        requestId
                );

        when(
                repository.findByTenantIdAndAiRequestId(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                Optional.of(audit)
        );

        service.attributeActor(
                tenantId,
                requestId,
                actorUserId
        );

        service.attributeActor(
                tenantId,
                requestId,
                actorUserId
        );

        assertEquals(
                actorUserId,
                audit.actorUserId()
        );
    }

    private static EaifExecutionAudit audit(
            UUID tenantId,
            UUID requestId
    ) {

        return new EaifExecutionAudit(
                tenantId,
                requestId,
                "GT_SCHOOL_TEACHER",
                "GT_TEXT_PRIMARY",
                null,
                AiEnums.RiskLevel.HIGH,
                null
        );
    }
}
