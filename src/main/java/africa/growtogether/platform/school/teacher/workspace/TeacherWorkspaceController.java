package africa.growtogether.platform.school.teacher.workspace;


import africa.growtogether.platform.school.teacher.workspace.api.TeacherWorkspaceResponse;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;



@RestController
@RequestMapping("/api/v1/school/teacher/workspace")
@PreAuthorize("denyAll()")
public class TeacherWorkspaceController {


    private final TeacherWorkspaceService service;


    public TeacherWorkspaceController(
            TeacherWorkspaceService service
    ) {

        this.service = service;

    }



    @GetMapping
    public TeacherWorkspaceResponse workspace(

            @RequestHeader("X-Tenant-ID")
            UUID tenantId,

            @RequestParam UUID teacherProfileId

    ) {


        return service.getWorkspace(
                tenantId,
                teacherProfileId
        );

    }


}
