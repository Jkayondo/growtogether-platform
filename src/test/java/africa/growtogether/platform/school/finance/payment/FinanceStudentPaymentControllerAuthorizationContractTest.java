package africa.growtogether.platform.school.finance.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class FinanceStudentPaymentControllerAuthorizationContractTest {

    @Test
    void freezesRouteAndPermissions() throws Exception {
        RequestMapping mapping =
                FinanceStudentPaymentController.class
                        .getAnnotation(
                                RequestMapping.class
                        );

        assertNotNull(mapping);

        assertTrue(
                contains(
                        mapping.value(),
                        "/api/v1/school/finance/payments"
                )
        );

        Method create =
                FinanceStudentPaymentController.class
                        .getMethod(
                                "create",
                                UUID.class,
                                FinanceStudentPaymentDtos.CreateRequest.class
                        );

        Method get =
                FinanceStudentPaymentController.class
                        .getMethod(
                                "get",
                                UUID.class,
                                UUID.class
                        );

        Method list =
                FinanceStudentPaymentController.class
                        .getMethod(
                                "listByFinancialAccount",
                                UUID.class,
                                UUID.class
                        );

        assertNotNull(
                create.getAnnotation(
                        PostMapping.class
                )
        );

        assertNotNull(
                get.getAnnotation(
                        GetMapping.class
                )
        );

        assertNotNull(
                list.getAnnotation(
                        GetMapping.class
                )
        );

        assertAuthority(
                create,
                "school.finance.manage"
        );

        assertAuthority(
                get,
                "school.finance.read"
        );

        assertAuthority(
                list,
                "school.finance.read"
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
                "hasAuthority('" + expected + "')",
                annotation.value()
        );
    }

    private static boolean contains(
            String[] values,
            String expected
    ) {
        for (String value : values) {
            if (expected.equals(value)) {
                return true;
            }
        }

        return false;
    }
}
