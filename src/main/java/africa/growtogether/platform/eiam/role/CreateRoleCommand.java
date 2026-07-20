package africa.growtogether.platform.eiam.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoleCommand(
    @NotBlank @Size(max = 100) String code,
    @NotBlank @Size(max = 150) String name,
    @Size(max = 500) String description,
    boolean systemRole) {}
