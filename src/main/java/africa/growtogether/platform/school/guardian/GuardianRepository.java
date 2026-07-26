package africa.growtogether.platform.school.guardian;

import java.util.Collection;
import java.util.UUID;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuardianRepository extends JpaRepository<Guardian, UUID> {

    boolean existsByGuardianNumber(String guardianNumber);

    long countByIdIn(Collection<UUID> ids);

}
