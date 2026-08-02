package africa.growtogether.platform.school.visitor.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;


public record VisitorRequestResponse(

        UUID id,

        UUID visitorId,

        UUID hostUserId,

        String purpose,

        LocalDate visitDate,

        LocalTime expectedArrivalTime,

        LocalTime expectedDepartureTime,

        String requestStatus,

        String approvalComment,

        String approvedBy

) {
}
