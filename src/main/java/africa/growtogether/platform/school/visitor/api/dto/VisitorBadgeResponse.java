package africa.growtogether.platform.school.visitor.api.dto;


import africa.growtogether.platform.school.visitor.domain.VisitorBadge;

import java.time.LocalDateTime;
import java.util.UUID;


public record VisitorBadgeResponse(

        UUID id,

        UUID visitorCheckInId,

        String badgeNumber,

        String badgeType,

        String badgeStatus,

        LocalDateTime issuedAt,

        String issuedBy,

        LocalDateTime returnedAt,

        String returnedBy

) {


    public static VisitorBadgeResponse from(
            VisitorBadge badge
    ) {

        return new VisitorBadgeResponse(

                badge.getId(),

                badge.getVisitorCheckInId(),

                badge.getBadgeNumber(),

                badge.getBadgeType(),

                badge.getBadgeStatus(),

                badge.getIssuedAt(),

                badge.getIssuedBy(),

                badge.getReturnedAt(),

                badge.getReturnedBy()

        );

    }

}
