package africa.growtogether.platform.eap;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricAggregateRepository
    extends JpaRepository<MetricAggregate, UUID> {

    Optional<MetricAggregate> findByTenantIdAndMetricCodeAndBucketStartAndDimensionHash(
        UUID tenantId,
        String code,
        Instant start,
        String hash
    );

    List<MetricAggregate> findByTenantIdAndMetricCodeAndBucketStartBetweenOrderByBucketStart(
        UUID tenantId,
        String code,
        Instant from,
        Instant to
    );
}