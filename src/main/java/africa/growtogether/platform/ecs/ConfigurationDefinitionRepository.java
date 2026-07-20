package africa.growtogether.platform.ecs;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface ConfigurationDefinitionRepository extends JpaRepository<ConfigurationDefinition,UUID>{Optional<ConfigurationDefinition> findByCodeIgnoreCase(String code);boolean existsByCodeIgnoreCase(String code);List<ConfigurationDefinition> findAllByOrderByCategoryAscCodeAsc();}
