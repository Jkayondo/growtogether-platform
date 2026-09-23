package africa.growtogether.platform.school.academic.learner360.learner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.school.learner.security.AuthenticatedLearnerResolver;
import africa.growtogether.platform.school.learner.security.AuthenticatedLearnerResolver.ResolvedLearner;
import africa.growtogether.platform.school.student.Student;

import java.lang.reflect.Method;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * L05C_AUTHENTICATED_LEARNER_SELF_RESOLUTION
 */
class LearnerSelfViewControllerTest {

    @Test
    void usesAuthenticatedResolvedLearnerOnly()
            throws Exception {

        LearnerSelfViewService service =
                org.mockito.Mockito.mock(
                        LearnerSelfViewService.class
                );

        AuthenticatedLearnerResolver learners =
                org.mockito.Mockito.mock(
                        AuthenticatedLearnerResolver.class
                );

        Student student =
                org.mockito.Mockito.mock(
                        Student.class
                );

        LearnerSelfView view =
                org.mockito.Mockito.mock(
                        LearnerSelfView.class
                );

        UUID tenantId =
                UUID.fromString(
                        "10000000-0000-0000-0000-000000000001"
                );

        UUID userId =
                UUID.fromString(
                        "20000000-0000-0000-0000-000000000001"
                );

        UUID learnerId =
                UUID.fromString(
                        "30000000-0000-0000-0000-000000000001"
                );

        when(student.getId())
                .thenReturn(learnerId);

        when(learners.requireCurrentLearner())
                .thenReturn(
                        new ResolvedLearner(
                                tenantId,
                                userId,
                                student
                        )
                );

        when(
                service.getLearnerView(
                        tenantId,
                        learnerId
                )
        ).thenReturn(view);

        LearnerSelfViewController controller =
                new LearnerSelfViewController(
                        service,
                        learners
                );

        assertThat(
                controller.getLearnerView()
        ).isSameAs(view);

        verify(learners)
                .requireCurrentLearner();

        verify(service)
                .getLearnerView(
                        tenantId,
                        learnerId
                );

        Method method =
                LearnerSelfViewController.class
                        .getDeclaredMethod(
                                "getLearnerView"
                        );

        assertThat(
                method.getParameterCount()
        ).isZero();

        GetMapping mapping =
                method.getAnnotation(
                        GetMapping.class
                );

        assertThat(mapping)
                .isNotNull();

        assertThat(mapping.value())
                .containsExactly("/me");

        PreAuthorize authorization =
                method.getAnnotation(
                        PreAuthorize.class
                );

        assertThat(authorization)
                .isNotNull();

        assertThat(
                authorization.value()
        ).isEqualTo(
                "isAuthenticated()"
        );
    }
}
