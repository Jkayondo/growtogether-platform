package africa.growtogether.platform.school.configuration.dto;


import java.util.UUID;


public record SchoolConfigurationResponse(

        UUID id,

        String schoolName,

        String countryCode,

        String schoolType

) {
}
