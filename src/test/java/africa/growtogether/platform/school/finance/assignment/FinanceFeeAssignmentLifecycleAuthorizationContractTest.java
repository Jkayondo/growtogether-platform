package africa.growtogether.platform.school.finance.assignment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Map;

class FinanceFeeAssignmentLifecycleAuthorizationContractTest {

    @Test
    void lifecycleEndpointsRequireFinanceApprovalAuthority() {

        Map<String, String> expected =
                Map.of(
                        "suspendStudentFeeAssignment",
                        "school.finance.approve",

                        "activateStudentFeeAssignment",
                        "school.finance.approve",

                        "completeStudentFeeAssignment",
                        "school.finance.approve",

                        "cancelStudentFeeAssignment",
                        "school.finance.approve",

                        "archiveStudentFeeAssignment",
                        "school.finance.approve"
                );

        for (
                Method method :
                FinanceFeeAssignmentController.class
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
                                                        FinanceFeeAssignmentController.class
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
