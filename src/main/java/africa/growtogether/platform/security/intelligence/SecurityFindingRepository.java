package africa.growtogether.platform.security.intelligence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SecurityFindingRepository
        extends JpaRepository<SecurityFinding, UUID> {
}
