package africa.growtogether.platform.school.academic.year;

import java.time.LocalDate;

public record CreateAcademicYearCommand(

        String academicYearCode,

        String academicYearName,

        LocalDate startDate,

        LocalDate endDate

) {
}