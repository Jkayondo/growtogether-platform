package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.error.GlobalExceptionHandler;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.common.security.GtPrincipal;
import africa.growtogether.platform.common.security.JwtAuthenticationFilter;
import africa.growtogether.platform.common.security.JwtService;
import africa.growtogether.platform.common.security.SecurityConfiguration;
import africa.growtogether.platform.common.security.SecurityErrorWriter;
import africa.growtogether.platform.common.security.TenantBoundaryFilter;
import africa.growtogether.platform.common.web.RequestContextFilter;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConnectController.class)
@EnableWebSecurity
@Import({
        ApiResponses.class,
        RequestContextFilter.class,
        GlobalExceptionHandler.class,
        SecurityErrorWriter.class,
        SecurityConfiguration.class,
        JwtAuthenticationFilter.class,
        TenantBoundaryFilter.class,
        EnterpriseIdentityContext.class,
        JwtService.class
})
@TestPropertySource(
        properties = {
                "gt.security.jwt.issuer=gt-test",
                "gt.security.jwt.secret=01234567890123456789012345678901",
                "gt.security.jwt.access-token-seconds=300"
        }
)
class ConnectControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private ConnectService service;

    @MockitoBean
    private ConnectMessageReceiptService receiptService;

    @MockitoBean
    private ConnectAnnouncementService announcementService;

    @Test
    void unauthenticatedConnectRequestIsRejected()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/connect/spaces/"
                                        + UUID.randomUUID()
                        )
                                .header(
                                        "X-Tenant-ID",
                                        UUID.randomUUID().toString()
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );

        verifyNoInteractions(
                service
        );
    }

    @Test
    void authenticatedRequestWithoutTenantHeaderIsRejected()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of()
                );

        mockMvc.perform(
                        get(
                                "/api/v1/connect/spaces/"
                                        + UUID.randomUUID()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-TENANT-001"
                                )
                );

        verifyNoInteractions(
                service
        );
    }

    @Test
    void crossTenantRequestIsRejectedBeforeService()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        principalTenant,
                        Set.of()
                );

        mockMvc.perform(
                        get(
                                "/api/v1/connect/spaces/"
                                        + UUID.randomUUID()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        requestedTenant.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-TENANT-002"
                                )
                );

        verifyNoInteractions(
                service
        );
    }

    @Test
    void managePermissionCanCreateSpace()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of(
                                ConnectPermissions.MANAGE
                        )
                );

        ConnectSpace space =
                space(
                        spaceId,
                        ConnectSpaceType.GROUP,
                        "Staff Room",
                        null,
                        null
                );

        when(
                service.createSpace(
                        ConnectSpaceType.GROUP,
                        "Staff Room",
                        null,
                        null
                )
        ).thenReturn(
                space
        );

        mockMvc.perform(
                        post(
                                "/api/v1/connect/spaces"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .header(
                                        "X-Correlation-ID",
                                        "GT-CONNECT-HTTP-001"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "spaceType": "GROUP",
                                          "name": "Staff Room"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-CONNECT-SPACE-001"
                                )
                )
                .andExpect(
                        jsonPath("$.data.id")
                                .value(
                                        spaceId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.metadata.tenantId")
                                .value(
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        header()
                                .string(
                                        "X-Correlation-ID",
                                        "GT-CONNECT-HTTP-001"
                                )
                );

        verify(
                service
        ).createSpace(
                ConnectSpaceType.GROUP,
                "Staff Room",
                null,
                null
        );
    }

    @Test
    void wrongPermissionCannotCreateSpace()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of(
                                ConnectPermissions.MODERATE
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/connect/spaces"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "spaceType": "GROUP",
                                          "name": "Not Allowed"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-AUTH-003"
                                )
                );

        verify(
                service,
                never()
        ).createSpace(
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void parentMembershipEndpointRequiresManagePermission()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID parentUserId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of(
                                ConnectPermissions.MODERATE
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/parents"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "userId": "%s"
                                        }
                                        """.formatted(
                                                parentUserId
                                        )
                                )
                )
                .andExpect(
                        status().isForbidden()
                );

        verify(
                service,
                never()
        ).addParentMember(
                any(),
                any(),
                any()
        );
    }

    @Test
    void teacherMembershipEndpointRequiresManagePermission()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID teacherUserId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of()
                );

        mockMvc.perform(
                        post(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/teachers"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "userId": "%s"
                                        }
                                        """.formatted(
                                                teacherUserId
                                        )
                                )
                )
                .andExpect(
                        status().isForbidden()
                );

        verify(
                service,
                never()
        ).addTeacherMember(
                any(),
                any(),
                any()
        );
    }

    @Test
    void authenticatedParticipantCanDelegateGetSpaceToService()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of()
                );

        ConnectSpace space =
                space(
                        spaceId,
                        ConnectSpaceType.GROUP,
                        "Community",
                        null,
                        null
                );

        when(
                service.getSpace(
                        spaceId
                )
        ).thenReturn(
                space
        );

        mockMvc.perform(
                        get(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-CONNECT-SPACE-002"
                                )
                );

        verify(
                service
        ).getSpace(
                spaceId
        );
    }

    @Test
    void unauthenticatedMessageSearchIsRejected()
            throws Exception {

        UUID spaceId =
                UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages/search"
                        )
                                .param(
                                        "q",
                                        "school fees"
                                )
                                .header(
                                        "X-Tenant-ID",
                                        UUID.randomUUID().toString()
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );

        verify(
                service,
                never()
        ).searchMessages(
                any(),
                any(),
                any()
        );
    }


    @Test
    void crossTenantMessageSearchIsRejectedBeforeService()
            throws Exception {

        UUID principalTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        principalTenant,
                        Set.of()
                );

        mockMvc.perform(
                        get(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages/search"
                        )
                                .param(
                                        "q",
                                        "school fees"
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        requestedTenant.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                );

        verify(
                service,
                never()
        ).searchMessages(
                any(),
                any(),
                any()
        );
    }


    @Test
    void messageSearchRequiresQueryParameter()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of()
                );

        mockMvc.perform(
                        get(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages/search"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(
                service,
                never()
        ).searchMessages(
                any(),
                any(),
                any()
        );
    }


    @Test
    void blankMessageSearchQueryIsRejectedAsBadRequest()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of()
                );

        mockMvc.perform(
                        get(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages/search"
                        )
                                .param(
                                        "q",
                                        "   "
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(
                service,
                never()
        ).searchMessages(
                any(),
                any(),
                any()
        );
    }


    @Test
    void authenticatedParticipantCanSearchMessagesWithExplicitLimit()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of()
                );

        ConnectMessage found =
                message(
                        messageId,
                        spaceId,
                        userId,
                        "School fees payment deadline is Friday"
                );

        when(
                service.searchMessages(
                        spaceId,
                        "school fees",
                        10
                )
        ).thenReturn(
                List.of(
                        found
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages/search"
                        )
                                .param(
                                        "q",
                                        "school fees"
                                )
                                .param(
                                        "limit",
                                        "10"
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-CONNECT-SEARCH-001"
                                )
                )
                .andExpect(
                        jsonPath("$.data.length()")
                                .value(
                                        1
                                )
                )
                .andExpect(
                        jsonPath("$.data[0].id")
                                .value(
                                        messageId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data[0].body")
                                .value(
                                        "School fees payment deadline is Friday"
                                )
                );

        verify(
                service
        ).searchMessages(
                spaceId,
                "school fees",
                10
        );
    }


    @Test
    void authenticatedParticipantCanSearchMessagesWithDefaultLimit()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of()
                );

        when(
                service.searchMessages(
                        spaceId,
                        "parents meeting",
                        null
                )
        ).thenReturn(
                List.of()
        );

        mockMvc.perform(
                        get(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages/search"
                        )
                                .param(
                                        "q",
                                        "parents meeting"
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-CONNECT-SEARCH-001"
                                )
                )
                .andExpect(
                        jsonPath("$.data.length()")
                                .value(
                                        0
                                )
                );

        verify(
                service
        ).searchMessages(
                spaceId,
                "parents meeting",
                null
        );
    }


    @Test
    void authenticatedParticipantCanDelegateMessageListToService()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        UUID documentId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of()
                );

        ConnectMessage message =
                mock(
                        ConnectMessage.class
                );

        when(
                message.getId()
        ).thenReturn(
                messageId
        );

        when(
                message.getSpaceId()
        ).thenReturn(
                spaceId
        );

        when(
                message.getSenderUserId()
        ).thenReturn(
                userId
        );

        when(
                message.getMessageType()
        ).thenReturn(
                ConnectMessageType.FILE
        );

        when(
                message.getBody()
        ).thenReturn(
                "School report"
        );

        ConnectMessageHistoryItem historyItem =
                new ConnectMessageHistoryItem(
                        message,
                        List.of(
                                new ConnectMessageHistoryItem.AttachmentMetadata(
                                        documentId,
                                        4,
                                        "application/pdf",
                                        32_000
                                )
                        )
                );

        when(
                service.listMessageHistory(
                        spaceId
                )
        ).thenReturn(
                List.of(
                        historyItem
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-CONNECT-MSG-002"
                                )
                )
                .andExpect(
                        jsonPath("$.data[0].id")
                                .value(
                                        messageId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data[0].messageType")
                                .value(
                                        "FILE"
                                )
                )
                .andExpect(
                        jsonPath("$.data[0].attachments.length()")
                                .value(
                                        1
                                )
                )
                .andExpect(
                        jsonPath("$.data[0].attachments[0].documentId")
                                .value(
                                        documentId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data[0].attachments[0].documentVersion")
                                .value(
                                        4
                                )
                )
                .andExpect(
                        jsonPath("$.data[0].attachments[0].mimeType")
                                .value(
                                        "application/pdf"
                                )
                )
                .andExpect(
                        jsonPath("$.data[0].attachments[0].sizeBytes")
                                .value(
                                        32_000
                                )
                );

        verify(
                service
        ).listMessageHistory(
                spaceId
        );

        verify(
                service,
                never()
        ).listMessages(
                spaceId
        );
    }

    @Test
    void authenticatedParticipantCanDelegateTextSendToService()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of()
                );

        ConnectMessage message =
                message(
                        messageId,
                        spaceId,
                        userId,
                        "Hello GT Connect"
                );

        when(
                service.sendTextMessage(
                        spaceId,
                        "Hello GT Connect",
                        null
                )
        ).thenReturn(
                message
        );

        mockMvc.perform(
                        post(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "body": "Hello GT Connect"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-CONNECT-MSG-001"
                                )
                )
                .andExpect(
                        jsonPath("$.data.messageType")
                                .value(
                                        "TEXT"
                                )
                );

        verify(
                service
        ).sendTextMessage(
                spaceId,
                "Hello GT Connect",
                null
        );
    }

    @Test
    void callerSuppliedTenantCannotOverrideAuthenticatedTenant()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID hostileTenant =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of(
                                ConnectPermissions.MANAGE
                        )
                );

        ConnectSpace createdSpace =
                space(
                        spaceId,
                        ConnectSpaceType.GROUP,
                        "Protected",
                        null,
                        null
                );

        when(
                service.createSpace(
                        ConnectSpaceType.GROUP,
                        "Protected",
                        null,
                        null
                )
        ).thenReturn(
                createdSpace
        );

        mockMvc.perform(
                        post(
                                "/api/v1/connect/spaces"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "tenantId": "%s",
                                          "spaceType": "GROUP",
                                          "name": "Protected"
                                        }
                                        """.formatted(
                                                hostileTenant
                                        )
                                )
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.metadata.tenantId")
                                .value(
                                        tenantId.toString()
                                )
                );

        verify(
                service
        ).createSpace(
                ConnectSpaceType.GROUP,
                "Protected",
                null,
                null
        );
    }

    @Test
    void callerSuppliedSenderCannotOverrideAuthenticatedSenderContract()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID authenticatedUser =
                UUID.randomUUID();

        UUID hostileSender =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        String token =
                token(
                        authenticatedUser,
                        tenantId,
                        Set.of()
                );

        ConnectMessage createdMessage =
                message(
                        UUID.randomUUID(),
                        spaceId,
                        authenticatedUser,
                        "Server decides sender"
                );

        when(
                service.sendTextMessage(
                        spaceId,
                        "Server decides sender",
                        null
                )
        ).thenReturn(
                createdMessage
        );

        mockMvc.perform(
                        post(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "senderUserId": "%s",
                                          "body": "Server decides sender"
                                        }
                                        """.formatted(
                                                hostileSender
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.data.senderUserId")
                                .value(
                                        authenticatedUser.toString()
                                )
                );

        verify(
                service
        ).sendTextMessage(
                spaceId,
                "Server decides sender",
                null
        );
    }

    @Test
    void unauthenticatedAttachmentSendIsRejected()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID documentId =
                UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages/attachments"
                        )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "documentId": "%s"
                                        }
                                        """.formatted(
                                                documentId
                                        )
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );

        verify(
                service,
                never()
        ).sendAttachmentMessage(
                any(),
                any(),
                any(),
                any()
        );
    }


    @Test
    void authenticatedParticipantCanDelegateAttachmentSendToService()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID documentId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of()
                );

        ConnectMessage message =
                mock(
                        ConnectMessage.class
                );

        when(
                message.getId()
        ).thenReturn(
                messageId
        );

        when(
                message.getSpaceId()
        ).thenReturn(
                spaceId
        );

        when(
                message.getSenderUserId()
        ).thenReturn(
                userId
        );

        when(
                message.getMessageType()
        ).thenReturn(
                ConnectMessageType.IMAGE
        );

        when(
                message.getBody()
        ).thenReturn(
                "School photograph"
        );

        when(
                service.sendAttachmentMessage(
                        spaceId,
                        documentId,
                        "School photograph",
                        null
                )
        ).thenReturn(
                message
        );

        mockMvc.perform(
                        post(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages/attachments"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "documentId": "%s",
                                          "body": "School photograph"
                                        }
                                        """.formatted(
                                                documentId
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-CONNECT-ATTACHMENT-001"
                                )
                )
                .andExpect(
                        jsonPath("$.data.id")
                                .value(
                                        messageId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.senderUserId")
                                .value(
                                        userId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.messageType")
                                .value(
                                        "IMAGE"
                                )
                );

        verify(
                service
        ).sendAttachmentMessage(
                spaceId,
                documentId,
                "School photograph",
                null
        );
    }


    @Test
    void attachmentSendRequiresDocumentId()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of()
                );

        mockMvc.perform(
                        post(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages/attachments"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "body": "Missing EDS document"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(
                service,
                never()
        ).sendAttachmentMessage(
                any(),
                any(),
                any(),
                any()
        );
    }


    @Test
    void unauthenticatedMessageEditIsRejected()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        mockMvc.perform(
                        patch(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages/"
                                        + messageId
                        )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "body": "Hostile edit"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );

        verifyNoInteractions(
                service
        );
    }

    @Test
    void authenticatedParticipantCanDelegateMessageEditToService()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of()
                );

        ConnectMessage editedMessage =
                message(
                        messageId,
                        spaceId,
                        userId,
                        "Edited through REST"
                );

        when(
                editedMessage.getEditedAt()
        ).thenReturn(
                Instant.parse(
                        "2026-08-23T18:00:00Z"
                )
        );

        when(
                service.editTextMessage(
                        spaceId,
                        messageId,
                        "Edited through REST"
                )
        ).thenReturn(
                editedMessage
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages/"
                                        + messageId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "body": "Edited through REST"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-CONNECT-MSG-003"
                                )
                )
                .andExpect(
                        jsonPath("$.data.body")
                                .value(
                                        "Edited through REST"
                                )
                );

        verify(
                service
        ).editTextMessage(
                spaceId,
                messageId,
                "Edited through REST"
        );
    }

    @Test
    void unauthenticatedMessageDeleteIsRejected()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        mockMvc.perform(
                        delete(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages/"
                                        + messageId
                        )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );

        verifyNoInteractions(
                service
        );
    }

    @Test
    void authenticatedDeleteMasksStoredMessageBody()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of()
                );

        Instant deletedAt =
                Instant.parse(
                        "2026-08-23T18:05:00Z"
                );

        /*
         * The domain still contains the original body for
         * evidence/audit purposes. MessageView must mask it.
         */
        ConnectMessage deletedMessage =
                message(
                        messageId,
                        spaceId,
                        userId,
                        "Stored audit evidence"
                );

        when(
                deletedMessage.getDeletedAt()
        ).thenReturn(
                deletedAt
        );

        when(
                service.deleteMessage(
                        spaceId,
                        messageId
                )
        ).thenReturn(
                deletedMessage
        );

        mockMvc.perform(
                        delete(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages/"
                                        + messageId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-CONNECT-MSG-004"
                                )
                )
                .andExpect(
                        jsonPath("$.data.body")
                                .value(
                                        org.hamcrest.Matchers.nullValue()
                                )
                )
                .andExpect(
                        jsonPath("$.data.deletedAt")
                                .value(
                                        deletedAt.toString()
                                )
                );

        verify(
                service
        ).deleteMessage(
                spaceId,
                messageId
        );
    }


    @Test
    void unauthenticatedMySpacesRetrievalIsRejected()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/connect/spaces"
                        )
                                .header(
                                        "X-Tenant-ID",
                                        UUID.randomUUID().toString()
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );

        verifyNoInteractions(
                service
        );
    }


    @Test
    void authenticatedCallerCanRetrieveMySpaces()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of()
                );

        ConnectSpace space =
                mock(
                        ConnectSpace.class
                );

        when(
                space.getId()
        ).thenReturn(
                spaceId
        );

        when(
                space.getSpaceType()
        ).thenReturn(
                ConnectSpaceType.GROUP
        );

        when(
                space.getName()
        ).thenReturn(
                "GT Connect Test Conversation"
        );

        when(
                space.getContextType()
        ).thenReturn(
                null
        );

        when(
                space.getContextReference()
        ).thenReturn(
                null
        );

        when(
                service.listMySpaces()
        ).thenReturn(
                List.of(
                        space
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/connect/spaces"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-CONNECT-SPACE-003"
                                )
                )
                .andExpect(
                        jsonPath("$.data.length()")
                                .value(
                                        1
                                )
                )
                .andExpect(
                        jsonPath("$.data[0].id")
                                .value(
                                        spaceId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data[0].spaceType")
                                .value(
                                        "GROUP"
                                )
                )
                .andExpect(
                        jsonPath("$.data[0].name")
                                .value(
                                        "GT Connect Test Conversation"
                                )
                );

        verify(
                service
        ).listMySpaces();
    }


    @Test
    void unauthenticatedReceiptStatusRetrievalIsRejected()
            throws Exception {

        UUID spaceId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages/"
                                        + messageId
                                        + "/receipts"
                        )
                                .header(
                                        "X-Tenant-ID",
                                        UUID.randomUUID().toString()
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );

        verifyNoInteractions(
                receiptService
        );
    }


    @Test
    void authenticatedCallerCanDelegateReceiptStatusRetrieval()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        UUID receiptId =
                UUID.randomUUID();

        UUID recipientUserId =
                UUID.randomUUID();

        Instant deliveredAt =
                Instant.parse(
                        "2026-08-28T12:00:00Z"
                );

        Instant readAt =
                Instant.parse(
                        "2026-08-28T12:05:00Z"
                );

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of()
                );

        ConnectMessageReceipt receipt =
                receipt(
                        receiptId,
                        messageId,
                        recipientUserId,
                        deliveredAt,
                        readAt
                );

        when(
                receiptService.getReceiptStatus(
                        spaceId,
                        messageId
                )
        ).thenReturn(
                List.of(
                        receipt
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages/"
                                        + messageId
                                        + "/receipts"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-CONNECT-RECEIPT-003"
                                )
                )
                .andExpect(
                        jsonPath("$.data.length()")
                                .value(
                                        1
                                )
                )
                .andExpect(
                        jsonPath("$.data[0].id")
                                .value(
                                        receiptId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data[0].messageId")
                                .value(
                                        messageId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data[0].userId")
                                .value(
                                        recipientUserId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data[0].deliveredAt")
                                .value(
                                        deliveredAt.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data[0].readAt")
                                .value(
                                        readAt.toString()
                                )
                );

        verify(
                receiptService
        ).getReceiptStatus(
                spaceId,
                messageId
        );
    }


    @Test
    void deniedReceiptStatusRetrievalReturnsForbidden()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of()
                );

        when(
                receiptService.getReceiptStatus(
                        spaceId,
                        messageId
                )
        ).thenThrow(
                new org.springframework.security.access.AccessDeniedException(
                        "Receipt status access denied"
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages/"
                                        + messageId
                                        + "/receipts"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-AUTH-003"
                                )
                );

        verify(
                receiptService
        ).getReceiptStatus(
                spaceId,
                messageId
        );
    }


    @Test
    void unauthenticatedDeliveredReceiptIsRejected()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages/"
                                        + messageId
                                        + "/delivered"
                        )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );

        verifyNoInteractions(
                receiptService
        );
    }

    @Test
    void authenticatedParticipantCanDelegateDeliveredReceipt()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        UUID receiptId =
                UUID.randomUUID();

        Instant deliveredAt =
                Instant.parse(
                        "2026-08-24T15:30:00Z"
                );

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of()
                );

        ConnectMessageReceipt receipt =
                receipt(
                        receiptId,
                        messageId,
                        userId,
                        deliveredAt,
                        null
                );

        when(
                receiptService.markDelivered(
                        spaceId,
                        messageId
                )
        ).thenReturn(
                receipt
        );

        mockMvc.perform(
                        post(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages/"
                                        + messageId
                                        + "/delivered"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-CONNECT-RECEIPT-001"
                                )
                )
                .andExpect(
                        jsonPath("$.data.id")
                                .value(
                                        receiptId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.messageId")
                                .value(
                                        messageId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.userId")
                                .value(
                                        userId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.deliveredAt")
                                .value(
                                        deliveredAt.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.readAt")
                                .value(
                                        org.hamcrest.Matchers.nullValue()
                                )
                );

        verify(
                receiptService
        ).markDelivered(
                spaceId,
                messageId
        );
    }

    @Test
    void authenticatedParticipantCanDelegateReadReceipt()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        UUID receiptId =
                UUID.randomUUID();

        Instant deliveredAt =
                Instant.parse(
                        "2026-08-24T15:31:00Z"
                );

        Instant readAt =
                Instant.parse(
                        "2026-08-24T15:32:00Z"
                );

        String token =
                token(
                        userId,
                        tenantId,
                        Set.of()
                );

        ConnectMessageReceipt receipt =
                receipt(
                        receiptId,
                        messageId,
                        userId,
                        deliveredAt,
                        readAt
                );

        when(
                receiptService.markRead(
                        spaceId,
                        messageId
                )
        ).thenReturn(
                receipt
        );

        mockMvc.perform(
                        post(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages/"
                                        + messageId
                                        + "/read"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-CONNECT-RECEIPT-002"
                                )
                )
                .andExpect(
                        jsonPath("$.data.messageId")
                                .value(
                                        messageId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.userId")
                                .value(
                                        userId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.deliveredAt")
                                .value(
                                        deliveredAt.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.readAt")
                                .value(
                                        readAt.toString()
                                )
                );

        verify(
                receiptService
        ).markRead(
                spaceId,
                messageId
        );
    }

    @Test
    void callerSuppliedReceiptIdentityCannotOverrideAuthenticatedIdentity()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID authenticatedUser =
                UUID.randomUUID();

        UUID hostileUser =
                UUID.randomUUID();

        UUID hostileTenant =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        Instant deliveredAt =
                Instant.parse(
                        "2026-08-24T15:33:00Z"
                );

        String token =
                token(
                        authenticatedUser,
                        tenantId,
                        Set.of()
                );

        ConnectMessageReceipt receipt =
                receipt(
                        UUID.randomUUID(),
                        messageId,
                        authenticatedUser,
                        deliveredAt,
                        null
                );

        when(
                receiptService.markDelivered(
                        spaceId,
                        messageId
                )
        ).thenReturn(
                receipt
        );

        mockMvc.perform(
                        post(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/messages/"
                                        + messageId
                                        + "/delivered"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "userId": "%s",
                                          "tenantId": "%s"
                                        }
                                        """.formatted(
                                                hostileUser,
                                                hostileTenant
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.data.userId")
                                .value(
                                        authenticatedUser.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.metadata.tenantId")
                                .value(
                                        tenantId.toString()
                                )
                );

        /*
         * The REST contract has no userId or tenantId argument.
         * Identity is derived beneath the HTTP boundary.
         */
        verify(
                receiptService
        ).markDelivered(
                spaceId,
                messageId
        );
    }


    @Test
    void announcementEndpointRequiresAnnouncementPermission()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of()
                );

        mockMvc.perform(
                        post(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/announcements"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "title": "Official Notice",
                                          "body": "Important information."
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-AUTH-003"
                                )
                );

        verifyNoInteractions(
                announcementService
        );
    }

    @Test
    void authorisedPublisherCanDelegateAnnouncementPublication()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID publisherUserId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID announcementId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        Instant publishedAt =
                Instant.parse(
                        "2026-08-24T19:20:00Z"
                );

        String token =
                token(
                        publisherUserId,
                        tenantId,
                        Set.of(
                                ConnectPermissions.SEND_ANNOUNCEMENT
                        )
                );

        ConnectAnnouncement announcement =
                announcement(
                        announcementId,
                        spaceId,
                        messageId,
                        publisherUserId,
                        "Official Notice",
                        "Important information.",
                        publishedAt
                );

        when(
                announcementService.publish(
                        spaceId,
                        "Official Notice",
                        "Important information."
                )
        ).thenReturn(
                announcement
        );

        mockMvc.perform(
                        post(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/announcements"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "title": "Official Notice",
                                          "body": "Important information."
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "GT-CONNECT-ANNOUNCEMENT-001"
                                )
                )
                .andExpect(
                        jsonPath("$.data.id")
                                .value(
                                        announcementId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.spaceId")
                                .value(
                                        spaceId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.messageId")
                                .value(
                                        messageId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.publishedByUserId")
                                .value(
                                        publisherUserId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.data.title")
                                .value(
                                        "Official Notice"
                                )
                )
                .andExpect(
                        jsonPath("$.data.publishedAt")
                                .value(
                                        publishedAt.toString()
                                )
                );

        verify(
                announcementService
        ).publish(
                spaceId,
                "Official Notice",
                "Important information."
        );
    }

    @Test
    void callerSuppliedAnnouncementIdentityCannotOverrideServerIdentity()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID authenticatedPublisher =
                UUID.randomUUID();

        UUID hostilePublisher =
                UUID.randomUUID();

        UUID hostileTenant =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID messageId =
                UUID.randomUUID();

        String token =
                token(
                        authenticatedPublisher,
                        tenantId,
                        Set.of(
                                ConnectPermissions.SEND_ANNOUNCEMENT
                        )
                );

        ConnectAnnouncement announcement =
                announcement(
                        UUID.randomUUID(),
                        spaceId,
                        messageId,
                        authenticatedPublisher,
                        "Server Authority",
                        "Identity is server controlled.",
                        Instant.parse(
                                "2026-08-24T19:21:00Z"
                        )
                );

        when(
                announcementService.publish(
                        spaceId,
                        "Server Authority",
                        "Identity is server controlled."
                )
        ).thenReturn(
                announcement
        );

        mockMvc.perform(
                        post(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/announcements"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "tenantId": "%s",
                                          "publishedByUserId": "%s",
                                          "messageId": "%s",
                                          "title": "Server Authority",
                                          "body": "Identity is server controlled."
                                        }
                                        """.formatted(
                                                hostileTenant,
                                                hostilePublisher,
                                                UUID.randomUUID()
                                        )
                                )
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.data.publishedByUserId")
                                .value(
                                        authenticatedPublisher.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.metadata.tenantId")
                                .value(
                                        tenantId.toString()
                                )
                );

        verify(
                announcementService
        ).publish(
                spaceId,
                "Server Authority",
                "Identity is server controlled."
        );
    }

    @Test
    void blankAnnouncementTitleIsRejectedBeforeService()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        String token =
                token(
                        UUID.randomUUID(),
                        tenantId,
                        Set.of(
                                ConnectPermissions.SEND_ANNOUNCEMENT
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/connect/spaces/"
                                        + spaceId
                                        + "/announcements"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-Tenant-ID",
                                        tenantId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "title": "   ",
                                          "body": "Valid body"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verifyNoInteractions(
                announcementService
        );
    }


    private String token(
            UUID userId,
            UUID tenantId,
            Set<String> permissions
    ) {

        GtPrincipal principal =
                new GtPrincipal(
                        userId,
                        "gt-connect-test-user",
                        tenantId,
                        Set.of(
                                "SCHOOL_ADMIN"
                        ),
                        permissions,
                        UUID.randomUUID()
                );

        return jwtService.issueAccessToken(
                principal
        );
    }

    private ConnectSpace space(
            UUID id,
            ConnectSpaceType type,
            String name,
            String contextType,
            String contextReference
    ) {

        ConnectSpace space =
                mock(
                        ConnectSpace.class
                );

        when(
                space.getId()
        ).thenReturn(
                id
        );

        when(
                space.getSpaceType()
        ).thenReturn(
                type
        );

        when(
                space.getName()
        ).thenReturn(
                name
        );

        when(
                space.getContextType()
        ).thenReturn(
                contextType
        );

        when(
                space.getContextReference()
        ).thenReturn(
                contextReference
        );

        return space;
    }

    private ConnectMessage message(
            UUID id,
            UUID spaceId,
            UUID senderUserId,
            String body
    ) {

        ConnectMessage message =
                mock(
                        ConnectMessage.class
                );

        when(
                message.getId()
        ).thenReturn(
                id
        );

        when(
                message.getSpaceId()
        ).thenReturn(
                spaceId
        );

        when(
                message.getSenderUserId()
        ).thenReturn(
                senderUserId
        );

        when(
                message.getMessageType()
        ).thenReturn(
                ConnectMessageType.TEXT
        );

        when(
                message.getBody()
        ).thenReturn(
                body
        );

        return message;
    }

    private ConnectMessageReceipt receipt(
            UUID id,
            UUID messageId,
            UUID userId,
            Instant deliveredAt,
            Instant readAt
    ) {

        ConnectMessageReceipt receipt =
                mock(
                        ConnectMessageReceipt.class
                );

        when(
                receipt.getId()
        ).thenReturn(
                id
        );

        when(
                receipt.getMessageId()
        ).thenReturn(
                messageId
        );

        when(
                receipt.getUserId()
        ).thenReturn(
                userId
        );

        when(
                receipt.getDeliveredAt()
        ).thenReturn(
                deliveredAt
        );

        when(
                receipt.getReadAt()
        ).thenReturn(
                readAt
        );

        return receipt;
    }


    private ConnectAnnouncement announcement(
            UUID id,
            UUID spaceId,
            UUID messageId,
            UUID publisherUserId,
            String title,
            String body,
            Instant publishedAt
    ) {

        ConnectAnnouncement announcement =
                mock(
                        ConnectAnnouncement.class
                );

        when(
                announcement.getId()
        ).thenReturn(
                id
        );

        when(
                announcement.getSpaceId()
        ).thenReturn(
                spaceId
        );

        when(
                announcement.getMessageId()
        ).thenReturn(
                messageId
        );

        when(
                announcement.getPublishedByUserId()
        ).thenReturn(
                publisherUserId
        );

        when(
                announcement.getTitle()
        ).thenReturn(
                title
        );

        when(
                announcement.getBody()
        ).thenReturn(
                body
        );

        when(
                announcement.getPublishedAt()
        ).thenReturn(
                publishedAt
        );

        return announcement;
    }

}
