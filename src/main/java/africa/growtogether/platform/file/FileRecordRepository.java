package africa.growtogether.platform.file;


import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;



public interface FileRecordRepository
        extends JpaRepository<FileRecord, UUID> {


    Optional<FileRecord> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );


}
