package africa.growtogether.platform.school.learner.security;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.school.student.Student;
import africa.growtogether.platform.school.student.StudentRepository;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * L05C_AUTHENTICATED_LEARNER_SELF_RESOLUTION
 *
 * Resolves learner identity exclusively from the authenticated
 * GT Enterprise identity. Learner self-service never accepts a
 * caller-selected learner identifier.
 */
@Service
public class AuthenticatedLearnerResolver {

    private final EnterpriseIdentityContext identity;
    private final StudentRepository students;

    public AuthenticatedLearnerResolver(
            EnterpriseIdentityContext identity,
            StudentRepository students
    ) {
        this.identity = identity;
        this.students = students;
    }

    @Transactional(readOnly = true)
    public ResolvedLearner requireCurrentLearner() {

        UUID tenantId =
                identity.requireTenantId();

        UUID userId =
                identity.requireUserId();

        List<Student> matches =
                students.findAllByTenantIdAndEiamUserId(
                        tenantId,
                        userId
                );

        if (matches.isEmpty()) {
            throw new AccessDeniedException(
                    "Authenticated user is not linked "
                    + "to a learner in this tenant."
            );
        }

        if (matches.size() != 1) {
            throw new AccessDeniedException(
                    "Authenticated learner identity is ambiguous."
            );
        }

        Student student = matches.get(0);

        if (
                student.getStudentStatus() == null
                || !"ACTIVE".equalsIgnoreCase(
                        student.getStudentStatus()
                )
        ) {
            throw new AccessDeniedException(
                    "Authenticated learner is not active."
            );
        }

        return new ResolvedLearner(
                tenantId,
                userId,
                student
        );
    }

    public record ResolvedLearner(
            UUID tenantId,
            UUID userId,
            Student student
    ) {
        public ResolvedLearner {
            if (
                    tenantId == null
                    || userId == null
                    || student == null
            ) {
                throw new IllegalArgumentException(
                        "Resolved learner identity must be complete."
                );
            }
        }
    }
}
