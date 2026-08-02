package africa.growtogether.platform.school.visitor.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;


public record CreateVisitorRequestRequest(

        @NotNull
        UUID visitorId,


        @NotNull
        UUID hostUserId,


        @NotBlank
        String purpose,


        @NotNull
        LocalDate visitDate,


        LocalTime expectedArrivalTime,


        LocalTime expectedDepartureTime

) {
}
