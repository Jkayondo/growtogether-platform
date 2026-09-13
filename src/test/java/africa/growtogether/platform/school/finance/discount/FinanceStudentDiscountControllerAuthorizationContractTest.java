package africa.growtogether.platform.school.finance.discount;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Map;

class FinanceStudentDiscountControllerAuthorizationContractTest {

    @Test
    void discountSchemeEndpointsCarryExplicitAuthorities() {

        RequestMapping mapping =
                FinanceStudentDiscountController.class
                        .getAnnotation(
                                RequestMapping.class
                        );

        assertNotNull(
                mapping
        );

        assertArrayEquals(
                new String[]{
                        "/api/v1/school/finance/student-discounts"
                },
                mapping.value()
        );

        Map<String, String> expected =
                Map.of(
                        "createStudentDiscountRequest",
                        "school.finance.manage",
                        "getStudentDiscountRequest",
                        "school.finance.read",
                        "listStudentDiscountRequests",
                        "school.finance.read"
                );

        for (
                Method method :
                FinanceStudentDiscountController.class
                        .getDeclaredMethods()
        ) {

            String authority =
                    expected.get(
                            method.getName()
                    );

            if (authority == null) {
                continue;
            }

            PreAuthorize annotation =
                    method.getAnnotation(
                            PreAuthorize.class
                    );

            assertNotNull(
                    annotation,
                    method.getName()
                            + " must declare @PreAuthorize"
            );

            assertEquals(
                    "hasAuthority('"
                            + authority
                            + "')",
                    annotation.value(),
                    method.getName()
            );
        }

        assertEquals(
                expected.size(),
                expected.keySet()
                        .stream()
                        .filter(
                                name ->
                                        java.util.Arrays
                                                .stream(
                                                        FinanceStudentDiscountController.class
                                                                .getDeclaredMethods()
                                                )
                                                .anyMatch(
                                                        method ->
                                                                method.getName()
                                                                        .equals(
                                                                                name
                                                                        )
                                                )
                        )
                        .count()
        );
    }
}
