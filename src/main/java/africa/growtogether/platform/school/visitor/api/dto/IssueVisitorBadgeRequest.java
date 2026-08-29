package africa.growtogether.platform.school.visitor.api.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;


public record IssueVisitorBadgeRequest(

        @NotNull
        UUID visitorCheckInId,


        @NotBlank
        String badgeNumber,


        String badgeType,


        @NotBlank
        String issuedBy

) {
}
