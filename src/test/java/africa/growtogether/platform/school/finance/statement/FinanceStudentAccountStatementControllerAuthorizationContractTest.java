package africa.growtogether.platform.school.finance.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class FinanceStudentAccountStatementControllerAuthorizationContractTest {

    @Test
    void accountSummaryRequiresFinanceRead() throws Exception {
        assertFinanceRead(
                "accountSummary",
                UUID.class,
                UUID.class,
                String.class,
                LocalDate.class
        );
    }

    @Test
    void accountStatementRequiresFinanceRead() throws Exception {
        assertFinanceRead(
                "accountStatement",
                UUID.class,
                UUID.class,
                String.class,
                LocalDate.class,
                LocalDate.class,
                LocalDate.class
        );
    }

    @Test
    void arrearsRequiresFinanceRead() throws Exception {
        assertFinanceRead(
                "arrears",
                UUID.class,
                UUID.class,
                String.class,
                LocalDate.class
        );
    }

    private void assertFinanceRead(
            String methodName,
            Class<?>... parameterTypes
    ) throws Exception {

        Method method =
                FinanceStudentAccountStatementController.class
                        .getDeclaredMethod(
                                methodName,
                                parameterTypes
                        );

        PreAuthorize annotation =
                method.getAnnotation(
                        PreAuthorize.class
                );

        assertNotNull(annotation);

        assertEquals(
                "hasAuthority('school.finance.read')",
                annotation.value()
        );
    }
}
