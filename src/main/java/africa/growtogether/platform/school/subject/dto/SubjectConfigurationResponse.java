package africa.growtogether.platform.school.subject.dto;


import java.util.UUID;


public record SubjectConfigurationResponse(

        UUID id,

        UUID academicGradeId,

        String subjectName,

        String subjectCode,

        boolean mandatory

) {
}
