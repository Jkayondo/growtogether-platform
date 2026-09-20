package africa.growtogether.platform.school.teacher.programme;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authenticated teacher self-service endpoint for Today Programme.
 *
 * Teacher identity and tenant identity are never caller-selected here.
 * They are resolved by the underlying authenticated programme services.
 */
@RestController
@RequestMapping("/api/v1/school/teacher/programme")
public class TeacherProgrammeController {

    private final TeacherProgrammeTodayService programme;

    public TeacherProgrammeController(
            TeacherProgrammeTodayService programme
    ) {
        this.programme = programme;
    }

    @GetMapping("/today")
    @PreAuthorize(
            "hasAuthority('school.teacher.programme.read')"
    )
    public TeacherProgrammeToday today() {

        return programme.currentProgramme();
    }
}
