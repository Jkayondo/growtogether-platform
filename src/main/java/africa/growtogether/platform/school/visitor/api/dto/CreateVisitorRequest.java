package africa.growtogether.platform.school.visitor.api.dto;

import jakarta.validation.constraints.NotBlank;


public record CreateVisitorRequest(

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        String phoneNumber,

        String email,

        @NotBlank(message = "Identification type is required")
        String identificationType,

        @NotBlank(message = "Identification reference is required")
        String identificationReference,

        @NotBlank(message = "Visitor category is required")
        String visitorCategory

) {
}
