package africa.growtogether.platform.connect;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConnectMessageRepository
        extends JpaRepository<ConnectMessage, UUID> {

    Optional<ConnectMessage> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );

    List<ConnectMessage>
    findAllByTenantIdAndSpaceIdOrderBySentAtAsc(
            UUID tenantId,
            UUID spaceId
    );

    List<ConnectMessage>
    findAllByTenantIdAndSenderUserIdOrderBySentAtDesc(
            UUID tenantId,
            UUID senderUserId
    );

    @Query(
            value = """
                    SELECT m.*
                    FROM gt_connect_messages m
                    WHERE m.tenant_id = :tenantId
                      AND m.space_id = :spaceId
                      AND m.deleted_at IS NULL
                      AND m.body IS NOT NULL
                      AND to_tsvector(
                            'simple',
                            coalesce(m.body, '')
                          )
                          @@ websearch_to_tsquery(
                                'simple',
                                :query
                          )
                    ORDER BY
                        ts_rank_cd(
                            to_tsvector(
                                'simple',
                                coalesce(m.body, '')
                            ),
                            websearch_to_tsquery(
                                'simple',
                                :query
                            )
                        ) DESC,
                        m.sent_at DESC
                    LIMIT :limit
                    """,
            nativeQuery = true
    )
    List<ConnectMessage> searchConversation(
            @Param("tenantId")
            UUID tenantId,

            @Param("spaceId")
            UUID spaceId,

            @Param("query")
            String query,

            @Param("limit")
            int limit
    );
}
