package africa.growtogether.platform.eds;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestParam;

class DocumentReferenceControllerAuthorizationContractTest {

    @Test
    void createRequiresExistingDocumentUpdatePermission()
            throws Exception {

        Method method =
                DocumentReferenceController.class
                        .getMethod(
                                "create",
                                DocumentReferenceController
                                        .CreateDocumentReferenceRequest.class
                        );

        PreAuthorize authorization =
                method.getAnnotation(
                        PreAuthorize.class
                );

        assertThat(authorization)
                .isNotNull();

        assertThat(authorization.value())
                .isEqualTo(
                        "hasAuthority('document.update')"
                );
    }

    @Test
    void lookupRequiresExistingDocumentUpdatePermission()
            throws Exception {

        Method method =
                DocumentReferenceController.class
                        .getMethod(
                                "findByReference",
                                String.class,
                                UUID.class
                        );

        PreAuthorize authorization =
                method.getAnnotation(
                        PreAuthorize.class
                );

        assertThat(authorization)
                .isNotNull();

        assertThat(authorization.value())
                .isEqualTo(
                        "hasAuthority('document.update')"
                );
    }

    @Test
    void publicControllerAcceptsNoTenantRequestParameter() {

        Arrays.stream(
                        DocumentReferenceController.class
                                .getDeclaredMethods()
                )
                .filter(
                        method ->
                                method.getName()
                                        .equals("create")
                                || method.getName()
                                        .equals("findByReference")
                )
                .forEach(
                        method -> {

                            boolean tenantRequestParameter =
                                    Arrays.stream(
                                                    method.getParameters()
                                            )
                                            .anyMatch(
                                                    parameter ->
                                                            parameter
                                                                    .isAnnotationPresent(
                                                                            RequestParam.class
                                                                    )
                                            );

                            assertThat(
                                    tenantRequestParameter
                            )
                                    .as(
                                            method.getName()
                                                    + " must not accept caller-controlled tenant identity"
                                    )
                                    .isFalse();
                        }
                );
    }
}
