package africa.growtogether.platform.eap;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AnalyticsEventRepository
    extends JpaRepository<AnalyticsEvent, UUID> {

    List<AnalyticsEvent> findTop100ByTenantIdAndProcessingStatusOrderByEventTimeAsc(
        UUID tenantId,
        AnalyticsEnums.EventStatus status
    );
}

interface MetricDefinitionRepository
    extends JpaRepository<MetricDefinition, UUID> {

    Optional<MetricDefinition> findByTenantIdAndMetricCode(
        UUID tenantId,
        String code
    );

    List<MetricDefinition> findByTenantIdAndEventTypeAndActiveTrue(
        UUID tenantId,
        String eventType
    );
}

interface DashboardDefinitionRepository
    extends JpaRepository<DashboardDefinition, UUID> {

    Optional<DashboardDefinition> findByTenantIdAndDashboardCode(
        UUID tenantId,
        String code
    );
}