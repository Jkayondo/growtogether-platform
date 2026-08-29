package africa.growtogether.platform.connect;

import java.util.List;
import java.util.UUID;

public record ConnectMessageHistoryItem(
        ConnectMessage message,
        List<AttachmentMetadata> attachments
) {

    public ConnectMessageHistoryItem {
        attachments =
                attachments == null
                        ? List.of()
                        : List.copyOf(
                                attachments
                        );
    }

    public record AttachmentMetadata(
            UUID documentId,
            int documentVersion,
            String mimeType,
            long sizeBytes
    ) {
    }
}
