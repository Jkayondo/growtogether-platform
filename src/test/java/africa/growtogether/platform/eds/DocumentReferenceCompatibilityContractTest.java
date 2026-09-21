package africa.growtogether.platform.eds;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class DocumentReferenceCompatibilityContractTest {

    @Test
    void preservesExistingAndCanonicalCreateSignatures()
            throws Exception {

        Method compatibility =
                DocumentReferenceService.class
                        .getMethod(
                                "create",
                                UUID.class,
                                UUID.class,
                                String.class,
                                UUID.class
                        );

        Method canonical =
                DocumentReferenceService.class
                        .getMethod(
                                "create",
                                UUID.class,
                                String.class,
                                UUID.class
                        );

        assertThat(
                compatibility.getReturnType()
        ).isEqualTo(
                DocumentReference.class
        );

        assertThat(
                canonical.getReturnType()
        ).isEqualTo(
                DocumentReference.class
        );
    }

    @Test
    void preservesExistingAndCanonicalLookupSignatures()
            throws Exception {

        Method compatibility =
                DocumentReferenceService.class
                        .getMethod(
                                "findByReference",
                                UUID.class,
                                String.class,
                                UUID.class
                        );

        Method canonical =
                DocumentReferenceService.class
                        .getMethod(
                                "findByReference",
                                String.class,
                                UUID.class
                        );

        assertThat(
                compatibility.getReturnType()
        ).isEqualTo(
                List.class
        );

        assertThat(
                canonical.getReturnType()
        ).isEqualTo(
                List.class
        );
    }
}
