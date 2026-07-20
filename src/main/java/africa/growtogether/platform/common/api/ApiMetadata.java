package africa.growtogether.platform.common.api;

import java.time.Instant;

public record ApiMetadata(String correlationId, String tenantId, Instant timestamp) {}
