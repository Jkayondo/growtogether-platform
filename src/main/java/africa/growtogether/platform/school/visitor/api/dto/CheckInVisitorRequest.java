package africa.growtogether.platform.school.visitor.api.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;


public record CheckInVisitorRequest(

        @NotNull(message = "Visitor request ID is required")
        UUID visitorRequestId,


        @NotBlank(message = "Gate location is required")
        String gateLocation,


        @NotBlank(message = "Badge number is required")
        String badgeNumber,


        @NotBlank(message = "Checked-in officer is required")
        String checkedInBy

) {
}
