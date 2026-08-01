package africa.growtogether.platform.school.visitor.api.dto;

import java.util.UUID;


public record VisitorResponse(

        UUID id,

        String firstName,

        String lastName,

        String phoneNumber,

        String email,

        String identificationType,

        String identificationReference,

        String visitorCategory

) {
}
