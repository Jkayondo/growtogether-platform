package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static africa.growtogether.platform.connect.ConnectDtos.*;

@Validated
@RestController
@RequestMapping("/api/v1/connect")
public class ConnectController {

    private final ConnectService service;
    private final ConnectMessageReceiptService receiptService;
    private final ConnectAnnouncementService announcementService;
    private final ApiResponses responses;

    public ConnectController(
            ConnectService service,
            ConnectMessageReceiptService receiptService,
            ConnectAnnouncementService announcementService,
            ApiResponses responses
    ) {
        this.service = service;
        this.receiptService = receiptService;
        this.announcementService = announcementService;
        this.responses = responses;
    }

    @GetMapping("/spaces")
    @PreAuthorize(
            "isAuthenticated()"
    )
    public ApiResponse<List<SpaceView>> listMySpaces() {

        List<SpaceView> views =
                service.listMySpaces()
                        .stream()
                        .map(
                                SpaceView::from
                        )
                        .toList();

        return responses.success(
                "GT-CONNECT-SPACE-003",
                "GT Connect spaces retrieved.",
                views
        );
    }


    @PostMapping("/spaces")
    @PreAuthorize(
            "hasAuthority('core.connect.manage')"
    )
    public ResponseEntity<ApiResponse<SpaceView>> createSpace(
            @Valid
            @RequestBody
            CreateSpaceCommand command
    ) {

        ConnectSpace space =
                service.createSpace(
                        command.spaceType(),
                        command.name(),
                        command.contextType(),
                        command.contextReference()
                );

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/v1/connect/spaces/"
                                        + space.getId()
                        )
                )
                .body(
                        responses.success(
                                "GT-CONNECT-SPACE-001",
                                "GT Connect space created.",
                                SpaceView.from(space)
                        )
                );
    }

    @GetMapping(
            "/spaces/{spaceId}/institution-member-candidates"
    )
    @PreAuthorize(
            "hasAuthority('core.connect.manage')"
    )
    public ApiResponse<
            List<ConnectDtos.InstitutionMemberCandidateView>
            > institutionMemberCandidates(
                    @PathVariable
                    UUID spaceId,

                    @RequestParam(required = false)
                    String query
            ) {

        return responses.success(
                "GT-CONNECT-MEMBER-004",
                "Institution member candidates retrieved.",
                service.searchInstitutionMemberCandidates(
                        spaceId,
                        query
                )
        );
    }


    @PostMapping(
            "/spaces/{spaceId}/institution-members"
    )
    @PreAuthorize(
            "hasAuthority('core.connect.manage')"
    )
    public ResponseEntity<ApiResponse<MemberView>> addInstitutionMember(
            @PathVariable
            UUID spaceId,

            @Valid
            @RequestBody
            AddProtectedMemberCommand command
    ) {

        ConnectSpaceMember member =
                service.addInstitutionMember(
                        spaceId,
                        command.userId()
                );

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/members/"
                                        + member.getId()
                        )
                )
                .body(
                        responses.success(
                                "GT-CONNECT-MEMBER-003",
                                "Member added to GT Connect institution space.",
                                MemberView.from(member)
                        )
                );
    }


    @PostMapping(
            "/spaces/{spaceId}/parents"
    )
    @PreAuthorize(
            "hasAuthority('core.connect.manage')"
    )
    public ResponseEntity<ApiResponse<MemberView>> addParent(
            @PathVariable
            UUID spaceId,

            @Valid
            @RequestBody
            AddProtectedMemberCommand command
    ) {

        ConnectSpaceMember member =
                service.addParentMember(
                        spaceId,
                        command.userId(),
                        ConnectMemberRole.MEMBER
                );

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/members/"
                                        + member.getId()
                        )
                )
                .body(
                        responses.success(
                                "GT-CONNECT-MEMBER-001",
                                "Parent added to GT Connect space.",
                                MemberView.from(member)
                        )
                );
    }

    @PostMapping(
            "/spaces/{spaceId}/teachers"
    )
    @PreAuthorize(
            "hasAuthority('core.connect.manage')"
    )
    public ResponseEntity<ApiResponse<MemberView>> addTeacher(
            @PathVariable
            UUID spaceId,

            @Valid
            @RequestBody
            AddProtectedMemberCommand command
    ) {

        ConnectSpaceMember member =
                service.addTeacherMember(
                        spaceId,
                        command.userId(),
                        ConnectMemberRole.MEMBER
                );

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/members/"
                                        + member.getId()
                        )
                )
                .body(
                        responses.success(
                                "GT-CONNECT-MEMBER-002",
                                "Teacher added to GT Connect space.",
                                MemberView.from(member)
                        )
                );
    }

    @GetMapping(
            "/spaces/{spaceId}"
    )
    @PreAuthorize(
            "isAuthenticated()"
    )
    public ApiResponse<SpaceView> getSpace(
            @PathVariable
            UUID spaceId
    ) {

        return responses.success(
                "GT-CONNECT-SPACE-002",
                "GT Connect space retrieved.",
                SpaceView.from(
                        service.getSpace(
                                spaceId
                        )
                )
        );
    }

    @GetMapping(
            "/spaces/{spaceId}/messages/search"
    )
    @PreAuthorize(
            "isAuthenticated()"
    )
    public ApiResponse<List<MessageView>> searchMessages(
            @PathVariable
            UUID spaceId,

            @RequestParam("q")
            @NotBlank
            @Size(max = 200)
            String query,

            @RequestParam(
                    value = "limit",
                    required = false
            )
            Integer limit
    ) {

        List<MessageView> views =
                service.searchMessages(
                                spaceId,
                                query,
                                limit
                        )
                        .stream()
                        .map(
                                MessageView::from
                        )
                        .toList();

        return responses.success(
                "GT-CONNECT-SEARCH-001",
                "GT Connect messages searched.",
                views
        );
    }


    @GetMapping(
            "/spaces/{spaceId}/messages"
    )
    @PreAuthorize(
            "isAuthenticated()"
    )
    public ApiResponse<List<MessageHistoryView>> listMessages(
            @PathVariable
            UUID spaceId
    ) {

        List<MessageHistoryView> views =
                service.listMessageHistory(
                                spaceId
                        )
                        .stream()
                        .map(
                                MessageHistoryView::from
                        )
                        .toList();

        return responses.success(
                "GT-CONNECT-MSG-002",
                "GT Connect messages retrieved.",
                views
        );
    }

    @PatchMapping(
            "/spaces/{spaceId}/messages/{messageId}"
    )
    @PreAuthorize(
            "isAuthenticated()"
    )
    public ApiResponse<MessageView> editTextMessage(
            @PathVariable
            UUID spaceId,

            @PathVariable
            UUID messageId,

            @Valid
            @RequestBody
            EditTextMessageCommand command
    ) {

        ConnectMessage message =
                service.editTextMessage(
                        spaceId,
                        messageId,
                        command.body()
                );

        return responses.success(
                "GT-CONNECT-MSG-003",
                "GT Connect message edited.",
                MessageView.from(
                        message
                )
        );
    }

    @DeleteMapping(
            "/spaces/{spaceId}/messages/{messageId}"
    )
    @PreAuthorize(
            "isAuthenticated()"
    )
    public ApiResponse<MessageView> deleteMessage(
            @PathVariable
            UUID spaceId,

            @PathVariable
            UUID messageId
    ) {

        ConnectMessage message =
                service.deleteMessage(
                        spaceId,
                        messageId
                );

        return responses.success(
                "GT-CONNECT-MSG-004",
                "GT Connect message deleted.",
                MessageView.from(
                        message
                )
        );
    }


    @GetMapping(
            "/spaces/{spaceId}/messages/{messageId}/receipts"
    )
    @PreAuthorize(
            "isAuthenticated()"
    )
    public ApiResponse<List<ReceiptView>> getMessageReceiptStatus(
            @PathVariable
            UUID spaceId,

            @PathVariable
            UUID messageId
    ) {

        List<ReceiptView> views =
                receiptService.getReceiptStatus(
                                spaceId,
                                messageId
                        )
                        .stream()
                        .map(
                                ReceiptView::from
                        )
                        .toList();

        return responses.success(
                "GT-CONNECT-RECEIPT-003",
                "GT Connect message receipt status retrieved.",
                views
        );
    }


    @PostMapping(
            "/spaces/{spaceId}/messages/{messageId}/delivered"
    )
    @PreAuthorize(
            "isAuthenticated()"
    )
    public ApiResponse<ReceiptView> markMessageDelivered(
            @PathVariable
            UUID spaceId,

            @PathVariable
            UUID messageId
    ) {

        ConnectMessageReceipt receipt =
                receiptService.markDelivered(
                        spaceId,
                        messageId
                );

        return responses.success(
                "GT-CONNECT-RECEIPT-001",
                "GT Connect message marked delivered.",
                ReceiptView.from(
                        receipt
                )
        );
    }


    @PostMapping(
            "/spaces/{spaceId}/messages/{messageId}/read"
    )
    @PreAuthorize(
            "isAuthenticated()"
    )
    public ApiResponse<ReceiptView> markMessageRead(
            @PathVariable
            UUID spaceId,

            @PathVariable
            UUID messageId
    ) {

        ConnectMessageReceipt receipt =
                receiptService.markRead(
                        spaceId,
                        messageId
                );

        return responses.success(
                "GT-CONNECT-RECEIPT-002",
                "GT Connect message marked read.",
                ReceiptView.from(
                        receipt
                )
        );
    }


    @PostMapping(
            "/spaces/{spaceId}/announcements"
    )
    @PreAuthorize(
            "hasAuthority('core.announcements.send')"
    )
    public ResponseEntity<ApiResponse<AnnouncementView>> publishAnnouncement(
            @PathVariable
            UUID spaceId,

            @Valid
            @RequestBody
            PublishAnnouncementCommand command
    ) {

        ConnectAnnouncement announcement =
                announcementService.publish(
                        spaceId,
                        command.title(),
                        command.body()
                );

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/announcements/"
                                        + announcement.getId()
                        )
                )
                .body(
                        responses.success(
                                "GT-CONNECT-ANNOUNCEMENT-001",
                                "GT Connect announcement published.",
                                AnnouncementView.from(
                                        announcement
                                )
                        )
                );
    }


    @PostMapping(
            "/spaces/{spaceId}/messages/attachments"
    )
    @PreAuthorize(
            "isAuthenticated()"
    )
    public ApiResponse<MessageView> sendAttachmentMessage(
            @PathVariable
            UUID spaceId,

            @Valid
            @RequestBody
            ConnectDtos.SendAttachmentMessageCommand command
    ) {

        ConnectMessage message =
                service.sendAttachmentMessage(
                        spaceId,
                        command.documentId(),
                        command.body(),
                        command.replyToMessageId()
                );

        return responses.success(
                "GT-CONNECT-ATTACHMENT-001",
                "GT Connect attachment message sent.",
                MessageView.from(
                        message
                )
        );
    }


    @PostMapping(
            "/spaces/{spaceId}/messages"
    )
    @PreAuthorize(
            "isAuthenticated()"
    )
    public ApiResponse<MessageView> sendTextMessage(
            @PathVariable
            UUID spaceId,

            @Valid
            @RequestBody
            SendTextMessageCommand command
    ) {

        ConnectMessage message =
                service.sendTextMessage(
                        spaceId,
                        command.body(),
                        command.replyToMessageId()
                );

        return responses.success(
                "GT-CONNECT-MSG-001",
                "GT Connect message sent.",
                MessageView.from(
                        message
                )
        );
    }
}
