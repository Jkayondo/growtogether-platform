package africa.growtogether.platform.school.visitor.api.dto;


import africa.growtogether.platform.school.visitor.domain.VisitorCheckIn;

import java.time.LocalDateTime;
import java.util.UUID;


public record VisitorCheckInResponse(

        UUID id,

        UUID visitorRequestId,

        UUID visitorId,

        String gateLocation,

        String badgeNumber,

        LocalDateTime checkedInAt,

        LocalDateTime checkedOutAt,

        String checkedInBy,

        String checkedOutBy,

        String checkInStatus

) {


    public static VisitorCheckInResponse from(
            VisitorCheckIn checkIn
    ) {

        return new VisitorCheckInResponse(

                checkIn.getId(),

                checkIn.getVisitorRequest().getId(),

                checkIn.getVisitor().getId(),

                checkIn.getGateLocation(),

                checkIn.getBadgeNumber(),

                checkIn.getCheckedInAt(),

                checkIn.getCheckedOutAt(),

                checkIn.getCheckedInBy(),

                checkIn.getCheckedOutBy(),

                checkIn.getCheckInStatus()

        );
    }
}
