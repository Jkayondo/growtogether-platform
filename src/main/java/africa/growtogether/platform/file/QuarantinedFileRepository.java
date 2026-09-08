package africa.growtogether.platform.file;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.UUID;



@Repository
public interface QuarantinedFileRepository
        extends JpaRepository<QuarantinedFile, UUID> {


    List<QuarantinedFile> findByTenantIdAndStatus(
            UUID tenantId,
            FileSecurityStatus status
    );


}
