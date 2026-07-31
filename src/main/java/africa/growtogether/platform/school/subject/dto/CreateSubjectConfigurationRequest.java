package africa.growtogether.platform.school.subject.dto;


import java.util.UUID;


public record CreateSubjectConfigurationRequest(

        UUID academicGradeId,

        String subjectName,

        String subjectCode,

        boolean mandatory

) {
}
