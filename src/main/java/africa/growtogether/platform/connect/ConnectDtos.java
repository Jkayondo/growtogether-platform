package africa.growtogether.platform.connect;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ConnectDtos {

    private ConnectDtos() {
    }

    public record CreateSpaceCommand(
            @NotNull
            ConnectSpaceType spaceType,

            @Size(max = 200)
            String name,

            @Size(max = 60)
            String contextType,

            @Size(max = 160)
            String contextReference
    ) {
    }

    public record AddProtectedMemberCommand(
            @NotNull
            UUID userId
    ) {
    }

    public record SendTextMessageCommand(
            @NotBlank
            String body,

            UUID replyToMessageId
    ) {
    }

    public record EditTextMessageCommand(
            @NotBlank
            String body
    ) {
    }


    public record SendAttachmentMessageCommand(
            @NotNull
            UUID documentId,

            String body,

            UUID replyToMessageId
    ) {
    }


    public record PublishAnnouncementCommand(
            @NotBlank
            @Size(max = 200)
            String title,

            @NotBlank
            String body
    ) {
    }

    public record SpaceView(
            UUID id,
            ConnectSpaceType spaceType,
            String name,
            String contextType,
            String contextReference
    ) {

        public static SpaceView from(
                ConnectSpace space
        ) {
            return new SpaceView(
                    space.getId(),
                    space.getSpaceType(),
                    space.getName(),
                    space.getContextType(),
                    space.getContextReference()
            );
        }
    }

    public record MemberView(
            UUID id,
            UUID spaceId,
            UUID userId,
            ConnectMemberRole memberRole,
            ConnectMembershipStatus membershipStatus,
            Instant joinedAt,
            Instant leftAt
    ) {

        public static MemberView from(
                ConnectSpaceMember member
        ) {
            return new MemberView(
                    member.getId(),
                    member.getSpaceId(),
                    member.getUserId(),
                    member.getMemberRole(),
                    member.getMembershipStatus(),
                    member.getJoinedAt(),
                    member.getLeftAt()
            );
        }
    }

    public record MessageView(
            UUID id,
            UUID spaceId,
            UUID senderUserId,
            ConnectMessageType messageType,
            String body,
            UUID replyToMessageId,
            Instant sentAt,
            Instant editedAt,
            Instant deletedAt
    ) {

        public static MessageView from(
                ConnectMessage message
        ) {
            return new MessageView(
                    message.getId(),
                    message.getSpaceId(),
                    message.getSenderUserId(),
                    message.getMessageType(),
                    message.getDeletedAt() == null
                            ? message.getBody()
                            : null,
                    message.getReplyToMessageId(),
                    message.getSentAt(),
                    message.getEditedAt(),
                    message.getDeletedAt()
            );
        }
    }

    public record AttachmentView(
            UUID documentId,
            int documentVersion,
            String mimeType,
            long sizeBytes
    ) {

        public static AttachmentView from(
                ConnectMessageHistoryItem.AttachmentMetadata attachment
        ) {
            return new AttachmentView(
                    attachment.documentId(),
                    attachment.documentVersion(),
                    attachment.mimeType(),
                    attachment.sizeBytes()
            );
        }
    }


    public record MessageHistoryView(
            UUID id,
            UUID spaceId,
            UUID senderUserId,
            ConnectMessageType messageType,
            String body,
            UUID replyToMessageId,
            Instant sentAt,
            Instant editedAt,
            Instant deletedAt,
            List<AttachmentView> attachments
    ) {

        public static MessageHistoryView from(
                ConnectMessageHistoryItem item
        ) {

            ConnectMessage message =
                    item.message();

            return new MessageHistoryView(
                    message.getId(),
                    message.getSpaceId(),
                    message.getSenderUserId(),
                    message.getMessageType(),
                    message.getDeletedAt() == null
                            ? message.getBody()
                            : null,
                    message.getReplyToMessageId(),
                    message.getSentAt(),
                    message.getEditedAt(),
                    message.getDeletedAt(),
                    item.attachments()
                            .stream()
                            .map(
                                    AttachmentView::from
                            )
                            .toList()
            );
        }
    }


    public record ReceiptView(
            UUID id,
            UUID messageId,
            UUID userId,
            Instant deliveredAt,
            Instant readAt
    ) {

        public static ReceiptView from(
                ConnectMessageReceipt receipt
        ) {
            return new ReceiptView(
                    receipt.getId(),
                    receipt.getMessageId(),
                    receipt.getUserId(),
                    receipt.getDeliveredAt(),
                    receipt.getReadAt()
            );
        }
    }


    public record AnnouncementView(
            UUID id,
            UUID spaceId,
            UUID messageId,
            UUID publishedByUserId,
            String title,
            String body,
            Instant publishedAt
    ) {

        public static AnnouncementView from(
                ConnectAnnouncement announcement
        ) {
            return new AnnouncementView(
                    announcement.getId(),
                    announcement.getSpaceId(),
                    announcement.getMessageId(),
                    announcement.getPublishedByUserId(),
                    announcement.getTitle(),
                    announcement.getBody(),
                    announcement.getPublishedAt()
            );
        }
    }

}
