package africa.growtogether.platform.school.student.identity;

import java.util.UUID;

/**
 * Generates GT controlled learner identifiers.
 *
 * Permanent Learner Number:
 * - lifelong GT learner identity
 * - globally unique
 * - immutable
 *
 * School Student Number:
 * - institution-specific identity
 * - generated per school/year sequence
 */
public interface LearnerIdentityGenerator {

    String generatePermanentLearnerNumber();

    String generateStudentNumber(
            UUID tenantId,
            String schoolCode,
            int admissionYear
    );

}
