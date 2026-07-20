package africa.growtogether.platform.common.web;

public record RequestContext(String correlationId, String tenantId) {
    public static final String CORRELATION_HEADER = "X-Correlation-ID";
    public static final String TENANT_HEADER = "X-Tenant-ID";
}
