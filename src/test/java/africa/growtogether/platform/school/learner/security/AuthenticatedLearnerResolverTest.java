package africa.growtogether.platform.school.learner.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.school.student.Student;
import africa.growtogether.platform.school.student.StudentRepository;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

/**
 * L05C_AUTHENTICATED_LEARNER_SELF_RESOLUTION
 */
@ExtendWith(MockitoExtension.class)
class AuthenticatedLearnerResolverTest {

    private static final UUID TENANT_ID =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000001"
            );

    private static final UUID USER_ID =
            UUID.fromString(
                    "20000000-0000-0000-0000-000000000001"
            );

    @Mock
    private EnterpriseIdentityContext identity;

    @Mock
    private StudentRepository students;

    @Mock
    private Student student;

    private AuthenticatedLearnerResolver resolver;

    @BeforeEach
    void setUp() {
        resolver =
                new AuthenticatedLearnerResolver(
                        identity,
                        students
                );
    }

    @Test
    void resolvesExactlyOneActiveLearner() {

        when(identity.requireTenantId())
                .thenReturn(TENANT_ID);

        when(identity.requireUserId())
                .thenReturn(USER_ID);

        when(
                students.findAllByTenantIdAndEiamUserId(
                        TENANT_ID,
                        USER_ID
                )
        ).thenReturn(List.of(student));

        when(student.getStudentStatus())
                .thenReturn("ACTIVE");

        var result =
                resolver.requireCurrentLearner();

        assertThat(result.tenantId())
                .isEqualTo(TENANT_ID);

        assertThat(result.userId())
                .isEqualTo(USER_ID);

        assertThat(result.student())
                .isSameAs(student);
    }

    @Test
    void deniesUserWithoutLearnerLink() {

        when(identity.requireTenantId())
                .thenReturn(TENANT_ID);

        when(identity.requireUserId())
                .thenReturn(USER_ID);

        when(
                students.findAllByTenantIdAndEiamUserId(
                        TENANT_ID,
                        USER_ID
                )
        ).thenReturn(List.of());

        assertThatThrownBy(
                resolver::requireCurrentLearner
        ).isInstanceOf(
                AccessDeniedException.class
        );
    }

    @Test
    void failsClosedForAmbiguousLearnerIdentity() {

        Student other =
                org.mockito.Mockito.mock(
                        Student.class
                );

        when(identity.requireTenantId())
                .thenReturn(TENANT_ID);

        when(identity.requireUserId())
                .thenReturn(USER_ID);

        when(
                students.findAllByTenantIdAndEiamUserId(
                        TENANT_ID,
                        USER_ID
                )
        ).thenReturn(
                List.of(
                        student,
                        other
                )
        );

        assertThatThrownBy(
                resolver::requireCurrentLearner
        ).isInstanceOf(
                AccessDeniedException.class
        );
    }

    @Test
    void deniesInactiveLearner() {

        when(identity.requireTenantId())
                .thenReturn(TENANT_ID);

        when(identity.requireUserId())
                .thenReturn(USER_ID);

        when(
                students.findAllByTenantIdAndEiamUserId(
                        TENANT_ID,
                        USER_ID
                )
        ).thenReturn(List.of(student));

        when(student.getStudentStatus())
                .thenReturn("INACTIVE");

        assertThatThrownBy(
                resolver::requireCurrentLearner
        ).isInstanceOf(
                AccessDeniedException.class
        );
    }
}
