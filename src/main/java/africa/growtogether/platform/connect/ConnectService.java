package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eds.integration.EdsDocumentAttachmentGateway;
import africa.growtogether.platform.eds.integration.EdsDocumentAttachmentGateway.AttachmentReference;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class ConnectService {

    private final ConnectSpaceRepository spaces;
    private final ConnectSpaceMemberRepository members;
    private final ConnectMessageRepository messages;

    private final ConnectMessageAttachmentRepository attachments;

    private final EdsDocumentAttachmentGateway edsAttachments;

    private final EnterpriseIdentityContext identity;
    private final ConnectParentRelationshipAuthorizationService parentAuthorization;
    private final ConnectTeacherAssignmentAuthorizationService teacherAssignmentAuthorization;

    public ConnectService(
            ConnectSpaceRepository spaces,
            ConnectSpaceMemberRepository members,
            ConnectMessageRepository messages,
            ConnectMessageAttachmentRepository attachments,
            EdsDocumentAttachmentGateway edsAttachments,
            EnterpriseIdentityContext identity,
            ConnectParentRelationshipAuthorizationService parentAuthorization,
            ConnectTeacherAssignmentAuthorizationService teacherAssignmentAuthorization
    ) {
        this.spaces = spaces;
        this.members = members;
        this.messages = messages;
        this.attachments = attachments;
        this.edsAttachments = edsAttachments;
        this.identity = identity;
        this.parentAuthorization = parentAuthorization;
        this.teacherAssignmentAuthorization = teacherAssignmentAuthorization;
    }

    @Transactional
    public ConnectSpace createSpace(
            ConnectSpaceType spaceType,
            String name,
            String contextType,
            String contextReference
    ) {
        requireManagePermission();

        UUID tenantId =
                identity.requireTenantId();

        UUID currentUserId =
                identity.requireUserId();

        ConnectSpace space =
                new ConnectSpace(
                        tenantId,
                        spaceType,
                        name,
                        contextType,
                        contextReference
                );

        ConnectSpace savedSpace =
                spaces.save(space);

        members.save(
                new ConnectSpaceMember(
                        tenantId,
                        savedSpace.getId(),
                        currentUserId,
                        ConnectMemberRole.OWNER
                )
        );

        return savedSpace;
    }

    @Transactional
    public ConnectSpaceMember addMember(
            UUID spaceId,
            UUID userId,
            ConnectMemberRole role
    ) {
        requireManagePermission();

        UUID tenantId =
                identity.requireTenantId();

        ConnectSpace space =
                requireSpace(
                        tenantId,
                        spaceId
                );

        if (isSchoolStudentContext(space)) {
            throw new AccessDeniedException(
                    "Learner-specific GT Connect membership must use relationship-aware authorization"
            );
        }

        if (isSchoolStreamContext(space)) {
            throw new AccessDeniedException(
                    "Stream-specific GT Connect membership must use teaching-assignment authorization"
            );
        }

        if (
                isSchoolProfileContext(space)
                        && role != ConnectMemberRole.MEMBER
        ) {
            throw new AccessDeniedException(
                    "Privileged membership in the canonical School "
                            + "institution GT Connect space is derived "
                            + "from authoritative EIAM School roles"
            );
        }

        return addMemberInternal(
                tenantId,
                spaceId,
                userId,
                role
        );
    }

    @Transactional
    public ConnectSpaceMember addParentMember(
            UUID spaceId,
            UUID parentUserId,
            ConnectMemberRole role
    ) {
        requireManagePermission();

        UUID tenantId =
                identity.requireTenantId();

        ConnectSpace space =
                requireSpace(
                        tenantId,
                        spaceId
                );

        requireProtectedParticipantRole(
                role
        );

        UUID studentId =
                requireSchoolStudentContext(
                        space
                );

        parentAuthorization
                .requireUserCanCommunicateWithStudent(
                        parentUserId,
                        studentId
                );

        return addMemberInternal(
                tenantId,
                spaceId,
                parentUserId,
                role
        );
    }

    @Transactional
    public ConnectSpaceMember addTeacherMember(
            UUID spaceId,
            UUID teacherUserId,
            ConnectMemberRole role
    ) {
        requireManagePermission();

        UUID tenantId =
                identity.requireTenantId();

        ConnectSpace space =
                requireSpace(
                        tenantId,
                        spaceId
                );

        requireProtectedParticipantRole(
                role
        );

        UUID streamId =
                requireSchoolStreamContext(
                        space
                );

        teacherAssignmentAuthorization
                .requireTeacherCanAccessStream(
                        teacherUserId,
                        streamId
                );

        return addMemberInternal(
                tenantId,
                spaceId,
                teacherUserId,
                role
        );
    }

    @Transactional
    public ConnectMessage sendTextMessage(
            UUID spaceId,
            String body,
            UUID replyToMessageId
    ) {
        UUID tenantId =
                identity.requireTenantId();

        UUID senderUserId =
                identity.requireUserId();

        ConnectSpace space =
                requireSpace(
                        tenantId,
                        spaceId
                );

        ConnectSpaceMember membership =
                requireActiveMember(
                        tenantId,
                        spaceId,
                        senderUserId
                );

        requireProtectedContextAccess(
                space,
                membership,
                senderUserId
        );

        if (replyToMessageId != null) {

            ConnectMessage repliedMessage =
                    messages
                            .findByIdAndTenantId(
                                    replyToMessageId,
                                    tenantId
                            )
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "Reply target message was not found"
                                    )
                            );

            if (
                    !spaceId.equals(
                            repliedMessage.getSpaceId()
                    )
            ) {
                throw new IllegalArgumentException(
                        "Reply target belongs to another GT Connect space"
                );
            }

            if (
                    repliedMessage.getDeletedAt()
                            != null
            ) {
                throw new IllegalArgumentException(
                        "Cannot reply to a deleted message"
                );
            }
        }

        ConnectMessage message =
                new ConnectMessage(
                        tenantId,
                        spaceId,
                        senderUserId,
                        ConnectMessageType.TEXT,
                        body,
                        replyToMessageId
                );

        return messages.save(message);
    }

    @Transactional
    public ConnectMessage sendAttachmentMessage(
            UUID spaceId,
            UUID documentId,
            String body,
            UUID replyToMessageId
    ) {

        UUID tenantId =
                identity.requireTenantId();

        UUID senderUserId =
                identity.requireUserId();

        ConnectSpace space =
                requireSpace(
                        tenantId,
                        spaceId
                );

        ConnectSpaceMember membership =
                requireActiveMember(
                        tenantId,
                        spaceId,
                        senderUserId
                );

        requireProtectedContextAccess(
                space,
                membership,
                senderUserId
        );

        /*
         * Attachment replies obey exactly the same R1 integrity
         * rules as text-message replies.
         */
        if (replyToMessageId != null) {

            ConnectMessage repliedMessage =
                    messages
                            .findByIdAndTenantId(
                                    replyToMessageId,
                                    tenantId
                            )
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "Reply target message was not found"
                                    )
                            );

            if (
                    !spaceId.equals(
                            repliedMessage.getSpaceId()
                    )
            ) {
                throw new IllegalArgumentException(
                        "Reply target belongs to another GT Connect space"
                );
            }

            if (
                    repliedMessage.getDeletedAt()
                            != null
            ) {
                throw new IllegalArgumentException(
                        "Cannot reply to a deleted message"
                );
            }
        }

        /*
         * EDS remains authoritative for:
         *
         * - document tenancy;
         * - read/security permissions;
         * - restricted sharing permission;
         * - lifecycle;
         * - immutable version;
         * - MIME type and size.
         */
        AttachmentReference reference =
                edsAttachments.requireAttachable(
                        documentId
                );

        ConnectMessageType messageType =
                attachmentMessageType(
                        reference.mimeType()
                );

        ConnectMessage message =
                new ConnectMessage(
                        tenantId,
                        spaceId,
                        senderUserId,
                        messageType,
                        body,
                        replyToMessageId
                );

        /*
         * Flush first because the attachment FK requires the
         * durable GT Connect message ID immediately.
         */
        ConnectMessage savedMessage =
                messages.saveAndFlush(
                        message
                );

        if (savedMessage.getId() == null) {
            throw new IllegalStateException(
                    "GT Connect attachment message was not assigned an ID"
            );
        }

        /*
         * Connect stores only the immutable EDS reference.
         * It never stores an EDS storage key or duplicate file bytes.
         */
        attachments.saveAndFlush(
                new ConnectMessageAttachment(
                        tenantId,
                        savedMessage.getId(),
                        reference.documentId(),
                        reference.versionNumber()
                )
        );

        return savedMessage;
    }


    @Transactional
    public ConnectMessage editTextMessage(
            UUID spaceId,
            UUID messageId,
            String newBody
    ) {

        UUID tenantId =
                identity.requireTenantId();

        UUID currentUserId =
                identity.requireUserId();

        ConnectMessage message =
                requireAccessibleMessage(
                        tenantId,
                        currentUserId,
                        spaceId,
                        messageId
                );

        if (
                !currentUserId.equals(
                        message.getSenderUserId()
                )
        ) {
            throw new AccessDeniedException(
                    "Only the original sender may edit a GT Connect message"
            );
        }

        if (
                message.getMessageType()
                        != ConnectMessageType.TEXT
        ) {
            throw new IllegalArgumentException(
                    "Only TEXT messages may be edited in GT Connect Release 1"
            );
        }

        message.edit(
                newBody
        );

        return messages.save(
                message
        );
    }


    @Transactional
    public ConnectMessage deleteMessage(
            UUID spaceId,
            UUID messageId
    ) {

        UUID tenantId =
                identity.requireTenantId();

        UUID currentUserId =
                identity.requireUserId();

        ConnectMessage message =
                requireAccessibleMessage(
                        tenantId,
                        currentUserId,
                        spaceId,
                        messageId
                );

        boolean sender =
                currentUserId.equals(
                        message.getSenderUserId()
                );

        boolean moderator =
                identity.hasPermission(
                        ConnectPermissions.MODERATE
                );

        if (
                !sender
                        && !moderator
        ) {
            throw new AccessDeniedException(
                    "Only the sender or an authorised GT Connect moderator may delete this message"
            );
        }

        message.delete();

        return messages.save(
                message
        );
    }


    @Transactional(readOnly = true)
    public List<ConnectMessage> listMessages(
            UUID spaceId
    ) {
        UUID tenantId =
                identity.requireTenantId();

        UUID currentUserId =
                identity.requireUserId();

        ConnectSpace space =
                requireSpace(
                        tenantId,
                        spaceId
                );

        ConnectSpaceMember membership =
                requireActiveMember(
                        tenantId,
                        spaceId,
                        currentUserId
                );

        requireProtectedContextAccess(
                space,
                membership,
                currentUserId
        );

        return messages
                .findAllByTenantIdAndSpaceIdOrderBySentAtAsc(
                        tenantId,
                        spaceId
                );
    }

    @Transactional(readOnly = true)
    public List<ConnectMessage> searchMessages(
            UUID spaceId,
            String query,
            Integer requestedLimit
    ) {

        UUID tenantId =
                identity.requireTenantId();

        UUID currentUserId =
                identity.requireUserId();

        ConnectSpace space =
                requireSpace(
                        tenantId,
                        spaceId
                );

        ConnectSpaceMember membership =
                requireActiveMember(
                        tenantId,
                        spaceId,
                        currentUserId
                );

        /*
         * Search must never bypass the same dynamic school-domain
         * authorization used when reading the conversation itself.
         */
        requireProtectedContextAccess(
                space,
                membership,
                currentUserId
        );

        if (
                query == null
                        || query.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "GT Connect search query is required"
            );
        }

        String normalizedQuery =
                query.trim();

        if (normalizedQuery.length() > 200) {
            throw new IllegalArgumentException(
                    "GT Connect search query must not exceed 200 characters"
            );
        }

        int limit =
                requestedLimit == null
                        ? 25
                        : requestedLimit;

        if (
                limit < 1
                        || limit > 50
        ) {
            throw new IllegalArgumentException(
                    "GT Connect search limit must be between 1 and 50"
            );
        }

        return messages.searchConversation(
                tenantId,
                spaceId,
                normalizedQuery,
                limit
        );
    }


    @Transactional(readOnly = true)
    public List<ConnectMessageHistoryItem> listMessageHistory(
            UUID spaceId
    ) {

        /*
         * Reuse the existing authoritative Connect access path.
         * It already enforces tenant, membership and protected
         * school-context authorization.
         */
        List<ConnectMessage> messageHistory =
                listMessages(
                        spaceId
                );

        if (messageHistory.isEmpty()) {
            return List.of();
        }

        UUID tenantId =
                identity.requireTenantId();

        List<UUID> messageIds =
                messageHistory
                        .stream()
                        .map(
                                ConnectMessage::getId
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .toList();

        if (messageIds.isEmpty()) {

            return messageHistory
                    .stream()
                    .map(
                            message ->
                                    new ConnectMessageHistoryItem(
                                            message,
                                            List.of()
                                    )
                    )
                    .toList();
        }

        List<ConnectMessageAttachment> attachmentLinks =
                attachments
                        .findAllByTenantIdAndMessageIdIn(
                                tenantId,
                                messageIds
                        );

        Map<UUID, List<ConnectMessageHistoryItem.AttachmentMetadata>>
                metadataByMessage =
                new HashMap<>();

        for (
                ConnectMessageAttachment attachment
                : attachmentLinks
        ) {

            /*
             * Resolve the exact historical EDS version recorded
             * when the message was sent.
             *
             * The gateway enforces current EDS read security but
             * never exposes storageKey.
             */
            AttachmentReference reference =
                    edsAttachments.requireReadableVersion(
                            attachment.getDocumentId(),
                            attachment.getDocumentVersion()
                    );

            metadataByMessage
                    .computeIfAbsent(
                            attachment.getMessageId(),
                            ignored ->
                                    new ArrayList<>()
                    )
                    .add(
                            new ConnectMessageHistoryItem.AttachmentMetadata(
                                    reference.documentId(),
                                    reference.versionNumber(),
                                    reference.mimeType(),
                                    reference.sizeBytes()
                            )
                    );
        }

        return messageHistory
                .stream()
                .map(
                        message ->
                                new ConnectMessageHistoryItem(
                                        message,
                                        metadataByMessage.getOrDefault(
                                                message.getId(),
                                                List.of()
                                        )
                                )
                )
                .toList();
    }


    @Transactional(readOnly = true)
    public List<ConnectSpace> listMySpaces() {

        UUID tenantId =
                identity.requireTenantId();

        UUID currentUserId =
                identity.requireUserId();

        List<ConnectSpaceMember> activeMemberships =
                members
                        .findAllByTenantIdAndUserIdAndMembershipStatus(
                                tenantId,
                                currentUserId,
                                ConnectMembershipStatus.ACTIVE
                        );

        List<ConnectSpace> accessibleSpaces =
                new ArrayList<>();

        for (
                ConnectSpaceMember membership
                        : activeMemberships
        ) {

            ConnectSpace space =
                    requireSpace(
                            tenantId,
                            membership.getSpaceId()
                    );

            try {

                /*
                 * Membership alone is not sufficient for protected
                 * School contexts.
                 *
                 * Parent relationships, teacher assignments and
                 * protected owner authority are revalidated here.
                 */
                requireProtectedContextAccess(
                        space,
                        membership,
                        currentUserId
                );

            }
            catch (
                    AccessDeniedException denied
            ) {

                /*
                 * A stale protected membership must not reveal the
                 * space and must not prevent other authorised
                 * conversations from being listed.
                 */
                continue;
            }

            accessibleSpaces.add(
                    space
            );
        }

        return List.copyOf(
                accessibleSpaces
        );
    }


    @Transactional(readOnly = true)
    public ConnectSpace getSpace(
            UUID spaceId
    ) {
        UUID tenantId =
                identity.requireTenantId();

        UUID currentUserId =
                identity.requireUserId();

        ConnectSpace space =
                requireSpace(
                        tenantId,
                        spaceId
                );

        ConnectSpaceMember membership =
                requireActiveMember(
                        tenantId,
                        spaceId,
                        currentUserId
                );

        requireProtectedContextAccess(
                space,
                membership,
                currentUserId
        );

        return space;
    }

    @Transactional
    public ConnectSpaceMember leaveSpace(
            UUID spaceId
    ) {
        UUID tenantId =
                identity.requireTenantId();

        UUID currentUserId =
                identity.requireUserId();

        requireSpace(
                tenantId,
                spaceId
        );

        ConnectSpaceMember membership =
                requireActiveMember(
                        tenantId,
                        spaceId,
                        currentUserId
                );

        if (
                membership.getMemberRole()
                        == ConnectMemberRole.OWNER
        ) {
            throw new AccessDeniedException(
                    "GT Connect space owner cannot leave until ownership is transferred"
            );
        }

        membership.leave();

        return members.save(
                membership
        );
    }

    @Transactional
    public ConnectSpaceMember removeMember(
            UUID spaceId,
            UUID userId
    ) {
        requireManagePermission();

        UUID tenantId =
                identity.requireTenantId();

        UUID currentUserId =
                identity.requireUserId();

        if (currentUserId.equals(userId)) {
            throw new IllegalArgumentException(
                    "Use leaveSpace to leave your own GT Connect space"
            );
        }

        requireSpace(
                tenantId,
                spaceId
        );

        ConnectSpaceMember membership =
                requireActiveMember(
                        tenantId,
                        spaceId,
                        userId
                );

        if (
                membership.getMemberRole()
                        == ConnectMemberRole.OWNER
        ) {
            throw new AccessDeniedException(
                    "GT Connect space owner cannot be removed until ownership is transferred"
            );
        }

        membership.remove();

        return members.save(
                membership
        );
    }

    private ConnectSpaceMember addMemberInternal(
            UUID tenantId,
            UUID spaceId,
            UUID userId,
            ConnectMemberRole role
    ) {
        if (
                members
                        .existsByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                                tenantId,
                                spaceId,
                                userId,
                                ConnectMembershipStatus.ACTIVE
                        )
        ) {
            throw new IllegalArgumentException(
                    "User is already an active member of the GT Connect space"
            );
        }

        return members.save(
                new ConnectSpaceMember(
                        tenantId,
                        spaceId,
                        userId,
                        role
                )
        );
    }

    private void requireProtectedParticipantRole(
            ConnectMemberRole role
    ) {
        if (role != ConnectMemberRole.MEMBER) {
            throw new AccessDeniedException(
                    "Protected GT Connect participants must use MEMBER role"
            );
        }
    }

    private void requireProtectedContextAccess(
            ConnectSpace space,
            ConnectSpaceMember membership,
            UUID currentUserId
    ) {

        boolean studentContext =
                isSchoolStudentContext(
                        space
                );

        boolean streamContext =
                isSchoolStreamContext(
                        space
                );

        if (!studentContext && !streamContext) {
            return;
        }

        ConnectMemberRole role =
                membership.getMemberRole();

        if (role == ConnectMemberRole.OWNER) {

            requireManagePermission();

            return;
        }

        if (role != ConnectMemberRole.MEMBER) {
            throw new AccessDeniedException(
                    "Protected GT Connect space role is not authorised"
            );
        }

        if (studentContext) {

            UUID studentId =
                    requireSchoolStudentContext(
                            space
                    );

            parentAuthorization
                    .requireUserCanCommunicateWithStudent(
                            currentUserId,
                            studentId
                    );

            return;
        }

        UUID streamId =
                requireSchoolStreamContext(
                        space
                );

        teacherAssignmentAuthorization
                .requireTeacherCanAccessStream(
                        currentUserId,
                        streamId
                );
    }

    private boolean isSchoolProfileContext(
            ConnectSpace space
    ) {

        return space.getContextType() != null
                && "SCHOOL_PROFILE"
                        .equalsIgnoreCase(
                                space.getContextType()
                        );
    }


    private boolean isSchoolStudentContext(
            ConnectSpace space
    ) {
        return space.getContextType() != null
                && ConnectContextTypes.SCHOOL_STUDENT
                        .equalsIgnoreCase(
                                space.getContextType()
                        );
    }

    private boolean isSchoolStreamContext(
            ConnectSpace space
    ) {
        return space.getContextType() != null
                && ConnectContextTypes.SCHOOL_STREAM
                        .equalsIgnoreCase(
                                space.getContextType()
                        );
    }

    private UUID requireSchoolStreamContext(
            ConnectSpace space
    ) {
        if (!isSchoolStreamContext(space)) {
            throw new IllegalArgumentException(
                    "GT Connect space is not a school stream space"
            );
        }

        String reference =
                space.getContextReference();

        if (
                reference == null
                || reference.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Stream-specific GT Connect space requires a stream reference"
            );
        }

        try {
            return UUID.fromString(
                    reference.trim()
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Stream-specific GT Connect space has an invalid stream reference",
                    exception
            );
        }
    }

    private UUID requireSchoolStudentContext(
            ConnectSpace space
    ) {
        if (!isSchoolStudentContext(space)) {
            throw new IllegalArgumentException(
                    "GT Connect space is not a learner-specific school space"
            );
        }

        String reference =
                space.getContextReference();

        if (
                reference == null
                || reference.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Learner-specific GT Connect space requires a student reference"
            );
        }

        try {
            return UUID.fromString(
                    reference.trim()
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Learner-specific GT Connect space has an invalid student reference",
                    exception
            );
        }
    }

    private static ConnectMessageType attachmentMessageType(
            String mimeType
    ) {

        String normalized =
                mimeType == null
                        ? ""
                        : mimeType
                                .trim()
                                .toLowerCase(
                                        Locale.ROOT
                                );

        if (normalized.startsWith("image/")) {
            return ConnectMessageType.IMAGE;
        }

        if (normalized.startsWith("audio/")) {
            return ConnectMessageType.AUDIO;
        }

        if (normalized.startsWith("video/")) {
            return ConnectMessageType.VIDEO;
        }

        return ConnectMessageType.FILE;
    }


    private ConnectMessage requireAccessibleMessage(
            UUID tenantId,
            UUID currentUserId,
            UUID spaceId,
            UUID messageId
    ) {

        ConnectSpace space =
                requireSpace(
                        tenantId,
                        spaceId
                );

        ConnectSpaceMember membership =
                requireActiveMember(
                        tenantId,
                        spaceId,
                        currentUserId
                );

        requireProtectedContextAccess(
                space,
                membership,
                currentUserId
        );

        ConnectMessage message =
                messages
                        .findByIdAndTenantId(
                                messageId,
                                tenantId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "GT Connect message was not found"
                                )
                        );

        if (
                !spaceId.equals(
                        message.getSpaceId()
                )
        ) {
            throw new IllegalArgumentException(
                    "GT Connect message belongs to another space"
            );
        }

        return message;
    }


    private ConnectSpace requireSpace(
            UUID tenantId,
            UUID spaceId
    ) {
        return spaces
                .findByIdAndTenantId(
                        spaceId,
                        tenantId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "GT Connect space was not found"
                        )
                );
    }

    private ConnectSpaceMember requireActiveMember(
            UUID tenantId,
            UUID spaceId,
            UUID userId
    ) {
        return members
                .findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                        tenantId,
                        spaceId,
                        userId,
                        ConnectMembershipStatus.ACTIVE
                )
                .orElseThrow(
                        () -> new AccessDeniedException(
                                "User is not an active member of this GT Connect space"
                        )
                );
    }

    private void requireManagePermission() {
        if (
                !identity.hasPermission(
                        ConnectPermissions.MANAGE
                )
        ) {
            throw new AccessDeniedException(
                    "GT Connect management permission is required"
            );
        }
    }
}
