package africa.growtogether.platform.school.visitor.api.dto;


import jakarta.validation.constraints.NotBlank;


public record ReturnVisitorBadgeRequest(

        @NotBlank
        String returnedBy

) {
}
