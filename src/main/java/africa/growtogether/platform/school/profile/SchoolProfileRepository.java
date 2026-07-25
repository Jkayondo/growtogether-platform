package africa.growtogether.platform.school.profile;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolProfileRepository
        extends JpaRepository<SchoolProfile, UUID> {

}
