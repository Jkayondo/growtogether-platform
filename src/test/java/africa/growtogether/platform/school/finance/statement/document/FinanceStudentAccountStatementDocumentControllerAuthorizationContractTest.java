package africa.growtogether.platform.school.finance.statement.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

class FinanceStudentAccountStatementDocumentControllerAuthorizationContractTest {

    @Test
    void controllerUsesEstablishedFinanceStudentBaseRoute() {

        RequestMapping mapping =
                FinanceStudentAccountStatementDocumentController.class
                        .getAnnotation(
                                RequestMapping.class
                        );

        assertThat(mapping)
                .isNotNull();

        assertThat(mapping.value())
                .containsExactly(
                        "/api/v1/school/finance/students/{studentId}"
                );
    }

    @Test
    void generationRequiresFinanceManagePermission()
            throws Exception {

        Method method =
                generationMethod();

        PreAuthorize authorization =
                method.getAnnotation(
                        PreAuthorize.class
                );

        assertThat(authorization)
                .isNotNull();

        assertThat(authorization.value())
                .isEqualTo(
                        "hasAuthority('school.finance.manage')"
                );
    }

    @Test
    void generationUsesReservedStatementDocumentEndpoint()
            throws Exception {

        PostMapping mapping =
                generationMethod()
                        .getAnnotation(
                                PostMapping.class
                        );

        assertThat(mapping)
                .isNotNull();

        assertThat(mapping.value())
                .containsExactly(
                        "/account-statement/document"
                );
    }

    @Test
    void generationHasNoRequestBodyOrRawRecipientContract()
            throws Exception {

        Method method =
                generationMethod();

        boolean requestBodyPresent =
                Arrays.stream(
                                method.getParameters()
                        )
                        .anyMatch(
                                parameter ->
                                        parameter
                                                .isAnnotationPresent(
                                                        RequestBody.class
                                                )
                        );

        assertThat(requestBodyPresent)
                .isFalse();

        assertThat(
                Arrays.stream(
                                method.getParameters()
                        )
                        .map(
                                parameter ->
                                        parameter.getName()
                        )
                        .toList()
        )
                .doesNotContain(
                        "recipient",
                        "email",
                        "phone"
                );
    }

    @Test
    void viewRequiresFinanceReadPermission()
            throws Exception {

        Method method =
                FinanceStudentAccountStatementDocumentController.class
                        .getMethod(
                                "viewDocument",
                                UUID.class,
                                UUID.class,
                                UUID.class
                        );

        PreAuthorize authorization =
                method.getAnnotation(
                        PreAuthorize.class
                );

        assertThat(authorization)
                .isNotNull();

        assertThat(authorization.value())
                .isEqualTo(
                        "hasAuthority('school.finance.read')"
                );

        GetMapping mapping =
                method.getAnnotation(
                        GetMapping.class
                );

        assertThat(mapping.value())
                .containsExactly(
                        "/account-statement/document/{statementReferenceId}"
                );
    }

    @Test
    void downloadRequiresFinanceReadPermission()
            throws Exception {

        Method method =
                FinanceStudentAccountStatementDocumentController.class
                        .getMethod(
                                "downloadDocument",
                                UUID.class,
                                UUID.class,
                                UUID.class
                        );

        PreAuthorize authorization =
                method.getAnnotation(
                        PreAuthorize.class
                );

        assertThat(authorization)
                .isNotNull();

        assertThat(authorization.value())
                .isEqualTo(
                        "hasAuthority('school.finance.read')"
                );

        GetMapping mapping =
                method.getAnnotation(
                        GetMapping.class
                );

        assertThat(mapping.value())
                .containsExactly(
                        "/account-statement/document/{statementReferenceId}/download"
                );
    }

    @Test
    void viewUsesPrivateNoStoreAndNosniffHeaders() {

        FinanceStudentAccountStatementDocumentService service =
                mock(
                        FinanceStudentAccountStatementDocumentService.class
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID statementReferenceId =
                UUID.randomUUID();

        when(
                service.retrieve(
                        tenantId,
                        studentId,
                        statementReferenceId
                )
        ).thenReturn(
                new FinanceStudentAccountStatementDocumentService.DocumentDownload(
                        new ByteArrayResource(
                                "statement".getBytes()
                        ),
                        "text/plain",
                        9L,
                        "statement.txt",
                        true
                )
        );

        FinanceStudentAccountStatementDocumentController controller =
                new FinanceStudentAccountStatementDocumentController(
                        service,
                        mock(FinanceStudentAccountStatementDeliveryService.class)
                );

        var response =
                controller.viewDocument(
                        tenantId,
                        studentId,
                        statementReferenceId
                );

        assertThat(
                response.getHeaders()
                        .getCacheControl()
        )
                .isEqualTo(
                        "private, no-store"
                );

        assertThat(
                response.getHeaders()
                        .getFirst(
                                "X-Content-Type-Options"
                        )
        )
                .isEqualTo(
                        "nosniff"
                );

        assertThat(
                response.getHeaders()
                        .getFirst(
                                "Content-Disposition"
                        )
        )
                .startsWith(
                        "inline"
                );
    }

    @Test
    void guardianDeliveryRequiresFinanceManagePermission()
            throws Exception {

        Method method =
                FinanceStudentAccountStatementDocumentController.class
                        .getMethod(
                                "deliver",
                                UUID.class,
                                UUID.class,
                                UUID.class,
                                FinanceStudentAccountStatementDocumentDtos
                                        .StatementDeliveryRequest.class
                        );

        PreAuthorize authorization =
                method.getAnnotation(
                        PreAuthorize.class
                );

        assertThat(authorization)
                .isNotNull();

        assertThat(authorization.value())
                .isEqualTo(
                        "hasAuthority('school.finance.manage')"
                );

        PostMapping mapping =
                method.getAnnotation(
                        PostMapping.class
                );

        assertThat(mapping)
                .isNotNull();

        assertThat(mapping.value())
                .containsExactly(
                        "/account-statement/document/{statementReferenceId}/deliver"
                );
    }

    @Test
    void guardianDeliveryRequestContainsGuardianIdAndChannelOnly() {

        assertThat(
                Arrays.stream(
                        FinanceStudentAccountStatementDocumentDtos
                                .StatementDeliveryRequest.class
                                .getRecordComponents()
                )
                        .map(
                                component ->
                                        component.getName()
                        )
                        .toList()
        )
                .containsExactly(
                        "guardianId",
                        "channel"
                )
                .doesNotContain(
                        "recipient",
                        "email",
                        "phone",
                        "storageKey",
                        "documentUrl"
                );
    }

    @Test
    void guardianDeliveryAcceptsOnlyEmailAndSms() {

        UUID guardianId =
                UUID.randomUUID();

        assertThatThrownBy(
                () ->
                        new FinanceStudentAccountStatementDocumentDtos
                                .StatementDeliveryRequest(
                                        guardianId,
                                        africa.growtogether.platform.ens.NotificationChannel.PUSH
                                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "EMAIL or SMS"
                );
    }

    private static Method generationMethod()
            throws Exception {

        return FinanceStudentAccountStatementDocumentController.class
                .getMethod(
                        "generate",
                        UUID.class,
                        UUID.class,
                        String.class,
                        LocalDate.class,
                        LocalDate.class,
                        LocalDate.class
                );
    }
}
