package africa.growtogether.platform.school.finance.discount;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Map;

class FinanceDiscountControllerAuthorizationContractTest {

    @Test
    void discountSchemeEndpointsCarryExplicitAuthorities() {

        RequestMapping mapping =
                FinanceDiscountController.class
                        .getAnnotation(
                                RequestMapping.class
                        );

        assertNotNull(
                mapping
        );

        assertArrayEquals(
                new String[]{
                        "/api/v1/school/finance/discount-schemes"
                },
                mapping.value()
        );

        Map<String, String> expected =
                Map.of(
                        "createFeeDiscountScheme",
                        "school.finance.manage",
                        "getFeeDiscountScheme",
                        "school.finance.read",
                        "listFeeDiscountSchemes",
                        "school.finance.read"
                );

        for (
                Method method :
                FinanceDiscountController.class
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
                                                        FinanceDiscountController.class
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
