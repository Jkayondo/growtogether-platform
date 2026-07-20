package africa.growtogether.platform.eiam.tenant;
import java.util.UUID;
public record TenantView(UUID organizationId,UUID tenantId,String organizationCode,String tenantCode,String tenantName,TenantStatus status,UUID administratorUserId,UUID administratorRoleId){}
