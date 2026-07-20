package africa.growtogether.platform.eiam.tenant;
import jakarta.validation.constraints.*;
public record ProvisionTenantCommand(@NotBlank String organizationCode,@NotBlank String organizationName,@NotBlank String tenantCode,@NotBlank String tenantName,@NotBlank @Email String administratorEmail,@NotBlank String administratorUsername,@NotBlank @Size(min=12,max=128) String administratorPassword,@NotBlank String administratorDisplayName){}
