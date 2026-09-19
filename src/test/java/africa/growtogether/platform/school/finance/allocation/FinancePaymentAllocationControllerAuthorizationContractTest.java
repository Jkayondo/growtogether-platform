
package africa.growtogether.platform.school.finance.allocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class FinancePaymentAllocationControllerAuthorizationContractTest {

    @Test
    void controllerUsesFrozenFinancePaymentResource() {
        RequestMapping mapping =
                FinancePaymentAllocationController.class
                        .getAnnotation(RequestMapping.class);

        assertNotNull(mapping);

        assertEquals(
                "/api/v1/school/finance/payments",
                mapping.value()[0]
        );
    }

    @Test
    void createRequiresFinanceManage() {
        Method method = method("create");

        assertEquals(
                "/{paymentId}/allocations",
                method.getAnnotation(
                        PostMapping.class
                ).value()[0]
        );

        assertEquals(
                "hasAuthority('school.finance.manage')",
                method.getAnnotation(
                        PreAuthorize.class
                ).value()
        );
    }

    @Test
    void readsRequireFinanceRead() {
        Method get = method("get");
        Method list = method("list");

        assertEquals(
                "/{paymentId}/allocations/{allocationId}",
                get.getAnnotation(
                        GetMapping.class
                ).value()[0]
        );

        assertEquals(
                "/{paymentId}/allocations",
                list.getAnnotation(
                        GetMapping.class
                ).value()[0]
        );

        assertEquals(
                "hasAuthority('school.finance.read')",
                get.getAnnotation(
                        PreAuthorize.class
                ).value()
        );

        assertEquals(
                "hasAuthority('school.finance.read')",
                list.getAnnotation(
                        PreAuthorize.class
                ).value()
        );
    }

    private static Method method(String name) {
        return Arrays.stream(
                        FinancePaymentAllocationController.class
                                .getDeclaredMethods()
                )
                .filter(candidate ->
                        candidate.getName().equals(name)
                )
                .findFirst()
                .orElseThrow();
    }

    @org.junit.jupiter.api.Test
    void correctionEndpointsPreserveFinanceAuthorizationBoundary()
            throws Exception {
        java.lang.reflect.Method reverse =
                FinancePaymentAllocationController.class.getMethod(
                        "reverse",
                        java.util.UUID.class,
                        java.util.UUID.class,
                        java.util.UUID.class,
                        FinancePaymentAllocationDtos.ReverseRequest.class
                );

        java.lang.reflect.Method reallocate =
                FinancePaymentAllocationController.class.getMethod(
                        "reallocate",
                        java.util.UUID.class,
                        java.util.UUID.class,
                        java.util.UUID.class,
                        FinancePaymentAllocationDtos.ReallocateRequest.class
                );

        java.lang.reflect.Method correction =
                FinancePaymentAllocationController.class.getMethod(
                        "getCorrection",
                        java.util.UUID.class,
                        java.util.UUID.class,
                        java.util.UUID.class
                );

        org.junit.jupiter.api.Assertions.assertEquals(
                "hasAuthority('school.finance.manage')",
                reverse.getAnnotation(
                        org.springframework.security.access.prepost.PreAuthorize.class
                ).value()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "hasAuthority('school.finance.manage')",
                reallocate.getAnnotation(
                        org.springframework.security.access.prepost.PreAuthorize.class
                ).value()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "hasAuthority('school.finance.read')",
                correction.getAnnotation(
                        org.springframework.security.access.prepost.PreAuthorize.class
                ).value()
        );
    }
}
