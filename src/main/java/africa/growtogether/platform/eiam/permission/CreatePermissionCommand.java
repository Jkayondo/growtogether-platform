package africa.growtogether.platform.eiam.permission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record CreatePermissionCommand(@NotBlank @Size(max=150) String code, @NotBlank @Size(max=150) String name, @NotBlank @Size(max=100) String module, @Size(max=500) String description, boolean systemPermission) {}
