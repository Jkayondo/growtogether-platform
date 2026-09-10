package africa.growtogether.platform.school.results.transcript;


import java.math.BigDecimal;
import java.util.UUID;


public record TranscriptEntry(

        UUID studentId,

        UUID academicYearId,

        UUID termId,

        UUID classId,

        UUID assessmentId,

        UUID subjectId,

        String subjectName,

        BigDecimal score,

        String gradeCode,

        String gradeName,

        BigDecimal gradePoint,

        UUID gradingSchemeId,

        UUID aggregationRuleId,

        UUID divisionRuleId

) {

}
