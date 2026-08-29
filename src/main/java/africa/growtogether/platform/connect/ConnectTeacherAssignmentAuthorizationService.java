package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import africa.growtogether.platform.school.academic.teaching.TeacherProfile;
import africa.growtogether.platform.school.academic.teaching.TeachingAssignment;
import africa.growtogether.platform.school.academic.teaching.TeachingAssignmentRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class ConnectTeacherAssignmentAuthorizationService {

    private final EnterpriseIdentityContext identity;
    private final ConnectTeacherAuthorizationService teacherAuthorization;
    private final TeachingAssignmentRepository assignments;

    public ConnectTeacherAssignmentAuthorizationService(
            EnterpriseIdentityContext identity,
            ConnectTeacherAuthorizationService teacherAuthorization,
            TeachingAssignmentRepository assignments
    ) {
        this.identity = identity;
        this.teacherAuthorization = teacherAuthorization;
        this.assignments = assignments;
    }

    @Transactional(readOnly = true)
    public TeachingAssignment requireCurrentTeacherCanAccessStream(
            UUID streamId
    ) {
        UUID authenticatedUserId =
                identity.requireUserId();

        return requireTeacherCanAccessStream(
                authenticatedUserId,
                streamId
        );
    }

    @Transactional(readOnly = true)
    public TeachingAssignment requireTeacherCanAccessStream(
            UUID userId,
            UUID streamId
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "userId must not be null"
            );
        }

        if (streamId == null) {
            throw new IllegalArgumentException(
                    "streamId must not be null"
            );
        }

        UUID tenantId =
                identity.requireTenantId();

        TeacherProfile teacher =
                teacherAuthorization
                        .requireTeacherProfileForUser(
                                userId
                        );

        LocalDate today =
                LocalDate.now();

        return assignments
                .findByTenantIdAndTeacherProfileId(
                        tenantId,
                        teacher.getId()
                )
                .stream()
                .filter(
                        assignment ->
                                "ACTIVE".equals(
                                        assignment.getAssignmentStatus()
                                )
                )
                .filter(
                        assignment ->
                                streamId.equals(
                                        assignment.getStreamId()
                                )
                )
                .filter(
                        assignment ->
                                assignment.getEffectiveFrom() != null
                                        && !assignment
                                                .getEffectiveFrom()
                                                .isAfter(today)
                )
                .filter(
                        assignment ->
                                assignment.getEffectiveTo() == null
                                        || !assignment
                                                .getEffectiveTo()
                                                .isBefore(today)
                )
                .findFirst()
                .orElseThrow(
                        ConnectTeacherAssignmentAuthorizationService::denied
                );
    }

    private static AccessDeniedException denied() {
        return new AccessDeniedException(
                "Teacher is not actively assigned to this stream"
        );
    }
}
