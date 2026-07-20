package africa.growtogether.platform.common.persistence;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "requestContextAuditorAware")
public class PersistenceAuditingConfiguration {
}
