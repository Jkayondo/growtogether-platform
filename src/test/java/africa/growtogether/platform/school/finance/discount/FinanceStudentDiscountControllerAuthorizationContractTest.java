package africa.growtogether.platform.school.finance.discount;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceStudentDiscountControllerAuthorizationContractTest {

    @Test
    void studentDiscountEndpointsCarryExplicitAuthorities() {
        RequestMapping mapping =
                FinanceStudentDiscountController.class.getAnnotation(
                        RequestMapping.class
                );

        assertNotNull(mapping);

        assertTrue(
                Arrays.asList(
                        mapping.value()
                ).contains(
                        "/api/v1/school/finance/student-discounts"
                )
        );

        assertAuthority(
                "createStudentDiscountRequest",
                "school.finance.manage"
        );

        assertAuthority(
                "getStudentDiscountRequest",
                "school.finance.read"
        );

        assertAuthority(
                "listStudentDiscountRequests",
                "school.finance.read"
        );

        assertAuthority(
                "approveStudentDiscountRequest",
                "school.finance.approve"
        );

        assertAuthority(
                "rejectStudentDiscountRequest",
                "school.finance.approve"
        );

        assertPostPath(
                "approveStudentDiscountRequest",
                "/{studentDiscountId}/approve"
        );

        assertPostPath(
                "rejectStudentDiscountRequest",
                "/{studentDiscountId}/reject"
        );
    }

    private static void assertAuthority(
            String methodName,
            String authority
    ) {
        Method method =
                findMethod(
                        methodName
                );

        PreAuthorize annotation =
                method.getAnnotation(
                        PreAuthorize.class
                );

        assertNotNull(annotation);

        assertEquals(
                "hasAuthority('" + authority + "')",
                annotation.value()
        );
    }

    private static void assertPostPath(
            String methodName,
            String path
    ) {
        Method method =
                findMethod(
                        methodName
                );

        PostMapping annotation =
                method.getAnnotation(
                        PostMapping.class
                );

        assertNotNull(annotation);

        assertTrue(
                Arrays.asList(
                        annotation.value()
                ).contains(
                        path
                )
        );
    }

    private static Method findMethod(
            String methodName
    ) {
        return Arrays.stream(
                        FinanceStudentDiscountController.class
                                .getDeclaredMethods()
                )
                .filter(
                        method ->
                                method.getName().equals(
                                        methodName
                                )
                )
                .findFirst()
                .orElseThrow();
    }
}
