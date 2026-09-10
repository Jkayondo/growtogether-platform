package africa.growtogether.platform.school.integration.teacher;


import java.util.UUID;


public record TeacherWorkflowSummary(

        UUID teacherId,

        int connectedCapabilities,

        String status

){

}
