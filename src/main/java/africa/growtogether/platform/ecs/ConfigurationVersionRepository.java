package africa.growtogether.platform.ecs;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ConfigurationVersionRepository extends JpaRepository<ConfigurationVersion,UUID>{
 List<ConfigurationVersion> findByConfigurationValueIdOrderByVersionNumberDesc(UUID configurationValueId);
 Optional<ConfigurationVersion> findByConfigurationValueIdAndVersionNumber(UUID configurationValueId,long versionNumber);
}
