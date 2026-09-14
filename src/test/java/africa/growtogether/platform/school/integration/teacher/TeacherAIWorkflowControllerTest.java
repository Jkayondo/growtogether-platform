package africa.growtogether.platform.school.integration.teacher;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.eaif.AiEnums;
import java.lang.reflect.Method;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class TeacherAIWorkflowControllerTest {

    @Test
    void submitDelegatesToGovernedWorkflowService() {
        TeacherAIWorkflowService service = mock(TeacherAIWorkflowService.class);
        TeacherAIWorkflowController controller =
                new TeacherAIWorkflowController(service);

        UUID tenantId = UUID.randomUUID();
        UUID teacherProfileId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        var expected = new TeacherAIWorkflowService.Submission(
                requestId,
                AiEnums.RequestStatus.SUCCEEDED);

        when(service.submit(
                tenantId,
                teacherProfileId,
                assignmentId,
                "teacher-model",
                "Explain this topic"))
                .thenReturn(expected);

        var actual = controller.submit(
                tenantId,
                new TeacherAIWorkflowController.SubmitRequest(
                        teacherProfileId,
                        assignmentId,
                        "teacher-model",
                        "Explain this topic"));

        assertEquals(expected, actual);

        verify(service).submit(
                tenantId,
                teacherProfileId,
                assignmentId,
                "teacher-model",
                "Explain this topic");
    }

    @Test
    void executeDelegatesToGovernedWorkflowService() {
        TeacherAIWorkflowService service = mock(TeacherAIWorkflowService.class);
        TeacherAIWorkflowController controller =
                new TeacherAIWorkflowController(service);

        UUID tenantId = UUID.randomUUID();
        UUID teacherProfileId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        var expected = new TeacherAIWorkflowService.RequestStatus(
                requestId,
                AiEnums.RequestStatus.SUCCEEDED,
                "document:output");

        when(service.execute(
                tenantId,
                teacherProfileId,
                assignmentId,
                requestId,
                "Approved input"))
                .thenReturn(expected);

        var actual = controller.execute(
                tenantId,
                requestId,
                new TeacherAIWorkflowController.ExecuteRequest(
                        teacherProfileId,
                        assignmentId,
                        "Approved input"));

        assertEquals(expected, actual);

        verify(service).execute(
                tenantId,
                teacherProfileId,
                assignmentId,
                requestId,
                "Approved input");
    }

    @Test
    void statusDelegatesToGovernedWorkflowService() {
        TeacherAIWorkflowService service = mock(TeacherAIWorkflowService.class);
        TeacherAIWorkflowController controller =
                new TeacherAIWorkflowController(service);

        UUID tenantId = UUID.randomUUID();
        UUID teacherProfileId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        var expected = new TeacherAIWorkflowService.RequestStatus(
                requestId,
                AiEnums.RequestStatus.SUCCEEDED,
                "document:output");

        when(service.status(
                tenantId,
                teacherProfileId,
                assignmentId,
                requestId))
                .thenReturn(expected);

        var actual = controller.status(
                tenantId,
                requestId,
                teacherProfileId,
                assignmentId);

        assertEquals(expected, actual);

        verify(service).status(
                tenantId,
                teacherProfileId,
                assignmentId,
                requestId);
    }

    @Test
    void exposesOnlyTheExpectedPermissionProtectedHttpContract()
            throws Exception {

        RequestMapping root =
                TeacherAIWorkflowController.class.getAnnotation(
                        RequestMapping.class);

        assertNotNull(root);
        assertArrayEquals(
                new String[]{"/api/v1/school/teacher/ai"},
                root.value());

        Method submit =
                TeacherAIWorkflowController.class.getDeclaredMethod(
                        "submit",
                        UUID.class,
                        TeacherAIWorkflowController.SubmitRequest.class);

        PostMapping submitRoute = submit.getAnnotation(PostMapping.class);
        assertNotNull(submitRoute);
        assertArrayEquals(
                new String[]{"/requests"},
                submitRoute.value());
        assertEquals(
                "hasAuthority('ai.request.create')",
                submit.getAnnotation(PreAuthorize.class).value());

        Method execute =
                TeacherAIWorkflowController.class.getDeclaredMethod(
                        "execute",
                        UUID.class,
                        UUID.class,
                        TeacherAIWorkflowController.ExecuteRequest.class);

        PostMapping executeRoute = execute.getAnnotation(PostMapping.class);
        assertNotNull(executeRoute);
        assertArrayEquals(
                new String[]{"/requests/{requestId}/execute"},
                executeRoute.value());
        assertEquals(
                "hasAuthority('ai.runtime.execute')",
                execute.getAnnotation(PreAuthorize.class).value());

        Method status =
                TeacherAIWorkflowController.class.getDeclaredMethod(
                        "status",
                        UUID.class,
                        UUID.class,
                        UUID.class,
                        UUID.class);

        GetMapping statusRoute = status.getAnnotation(GetMapping.class);
        assertNotNull(statusRoute);
        assertArrayEquals(
                new String[]{"/requests/{requestId}"},
                statusRoute.value());
        assertEquals(
                "hasAuthority('ai.request.read')",
                status.getAnnotation(PreAuthorize.class).value());
    }
}
