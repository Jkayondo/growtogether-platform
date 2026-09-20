package africa.growtogether.platform.school.finance.receipt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class FinancePaymentReceiptControllerAuthorizationContractTest {

    @Test
    void controllerUsesFinanceBaseRoute() {

        RequestMapping mapping =
                FinancePaymentReceiptController.class
                        .getAnnotation(RequestMapping.class);

        assertNotNull(mapping);

        assertArrayEquals(
                new String[]{
                        "/api/v1/school/finance"
                },
                mapping.value()
        );
    }

    @Test
    void issueUsesExistingFinanceManagePermission()
            throws Exception {

        Method issue =
                FinancePaymentReceiptController.class
                        .getMethod(
                                "issue",
                                UUID.class,
                                UUID.class
                        );

        assertAuthority(
                issue,
                "school.finance.manage"
        );

        PostMapping mapping =
                issue.getAnnotation(
                        PostMapping.class
                );

        assertNotNull(mapping);

        assertArrayEquals(
                new String[]{
                        "/payments/{paymentId}/receipt"
                },
                mapping.value()
        );
    }

    @Test
    void retrievalUsesExistingFinanceReadPermission()
            throws Exception {

        Method byPayment =
                FinancePaymentReceiptController.class
                        .getMethod(
                                "getByPayment",
                                UUID.class,
                                UUID.class
                        );

        Method byId =
                FinancePaymentReceiptController.class
                        .getMethod(
                                "getById",
                                UUID.class,
                                UUID.class
                        );

        assertAuthority(
                byPayment,
                "school.finance.read"
        );

        assertAuthority(
                byId,
                "school.finance.read"
        );

        assertArrayEquals(
                new String[]{
                        "/payments/{paymentId}/receipt"
                },
                byPayment
                        .getAnnotation(
                                GetMapping.class
                        )
                        .value()
        );

        assertArrayEquals(
                new String[]{
                        "/receipts/{receiptId}"
                },
                byId
                        .getAnnotation(
                                GetMapping.class
                        )
                        .value()
        );
    }

    private static void assertAuthority(
            Method method,
            String expected
    ) {

        PreAuthorize annotation =
                method.getAnnotation(
                        PreAuthorize.class
                );

        assertNotNull(annotation);

        assertEquals(
                "hasAuthority('"
                + expected
                + "')",
                annotation.value()
        );
    }
}
