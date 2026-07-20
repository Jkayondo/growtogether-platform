package africa.growtogether.platform.eap;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportExecutionRepository
    extends JpaRepository<ReportExecution, UUID> {
}