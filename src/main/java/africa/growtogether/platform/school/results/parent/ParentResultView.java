package africa.growtogether.platform.school.results.parent;


import java.util.List;
import java.util.UUID;


public record ParentResultView(

        UUID learnerId,

        UUID academicTermId,

        String termName,

        Double averageScore,

        String overallGrade,

        List<SubjectResultView> subjects

) {


    public record SubjectResultView(

            String subjectName,

            Double finalScore,

            String gradeCode,

            String gradeName,

            Double gradePoint,

            String teacherComment

    ){

    }

}
