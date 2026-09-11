package africa.growtogether.platform.school.finance.foundation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Map;


class FinanceFoundationControllerAuthorizationContractTest {

    @Test
    void financeEndpointsCarryExplicitAuthorities() {

        RequestMapping mapping =
                FinanceFoundationController.class
                        .getAnnotation(
                                RequestMapping.class
                        );


        assertNotNull(mapping);

        assertArrayEquals(
                new String[]{
                        "/api/v1/school/finance"
                },
                mapping.value()
        );


        Map<String, String> expected =
                Map.ofEntries(
                        Map.entry(
                                "createFeeCategory",
                                "school.finance.manage"
                        ),
                        Map.entry(
                                "listFeeCategories",
                                "school.finance.read"
                        ),
                        Map.entry(
                                "createFeeItem",
                                "school.finance.manage"
                        ),
                        Map.entry(
                                "listFeeItems",
                                "school.finance.read"
                        ),
                        Map.entry(
                                "createFeeStructure",
                                "school.finance.manage"
                        ),
                        Map.entry(
                                "listFeeStructures",
                                "school.finance.read"
                        ),
                        Map.entry(
                                "addFeeStructureItem",
                                "school.finance.manage"
                        ),
                        Map.entry(
                                "listFeeStructureItems",
                                "school.finance.read"
                        ),
                        Map.entry(
                                "approveFeeStructure",
                                "school.finance.approve"
                        ),
                        Map.entry(
                                "activateFeeStructure",
                                "school.finance.approve"
                        ),
                        Map.entry(
                                "openStudentAccount",
                                "school.finance.manage"
                        ),
                        Map.entry(
                                "findStudentAccount",
                                "school.finance.read"
                        )
                );


        for (Method method :
                FinanceFoundationController.class
                        .getDeclaredMethods()) {

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
                                                        FinanceFoundationController.class
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
