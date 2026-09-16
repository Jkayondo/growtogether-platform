package africa.growtogether.platform.eiam.tenant;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    List<Tenant> findAllByOrganizationIdOrderByNameAsc(
            UUID organizationId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select t
            from Tenant t
            where t.id = :id
            """)
    Optional<Tenant> findByIdForUpdate(
            @Param("id") UUID id
    );
}
