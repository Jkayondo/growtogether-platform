package africa.growtogether.platform.file;


import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;



public interface FileAccessRepository
        extends JpaRepository<FileAccess, UUID> {


    Optional<FileAccess> findByTokenHash(
            String tokenHash
    );


    Optional<FileAccess> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );

}
