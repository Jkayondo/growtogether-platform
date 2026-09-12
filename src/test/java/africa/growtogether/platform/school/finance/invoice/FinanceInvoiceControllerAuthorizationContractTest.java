package africa.growtogether.platform.school.finance.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import africa.growtogether.platform.school.finance.invoice.FinanceInvoiceDtos.CreateDraftInvoiceRequest;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.UUID;

class FinanceInvoiceControllerAuthorizationContractTest {

    @Test
    void invoiceEndpointsUseExistingFinanceAuthorities()
            throws Exception {

        assertAuthority(
                FinanceInvoiceController.class.getDeclaredMethod(
                        "createDraftInvoice",
                        UUID.class,
                        CreateDraftInvoiceRequest.class
                ),
                "hasAuthority('school.finance.manage')"
        );

        assertAuthority(
                FinanceInvoiceController.class.getDeclaredMethod(
                        "findStudentInvoice",
                        UUID.class,
                        UUID.class
                ),
                "hasAuthority('school.finance.read')"
        );

        assertAuthority(
                FinanceInvoiceController.class.getDeclaredMethod(
                        "listStudentInvoices",
                        UUID.class,
                        UUID.class
                ),
                "hasAuthority('school.finance.read')"
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

        assertEquals(
                expected,
                annotation.value()
        );
    }
}
