package africa.growtogether.platform.school.configuration.dto;


public record CreateSchoolConfigurationRequest(

        String schoolName,

        String countryCode,

        String schoolType

) {
}
