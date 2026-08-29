package africa.growtogether.platform.eip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExternalProviderAdapterRegistryTest {

    @Test
    void resolvesAdapterByNormalizedConnectorType() {
        ExternalProviderAdapter adapter =
                adapter("whatsapp");

        ExternalProviderAdapterRegistry registry =
                new ExternalProviderAdapterRegistry(
                        List.of(adapter)
                );

        assertThat(
                registry.require(
                        "  WHATSAPP  "
                )
        ).isSameAs(adapter);
    }

    @Test
    void rejectsMissingAdapter() {
        ExternalProviderAdapterRegistry registry =
                new ExternalProviderAdapterRegistry(
                        List.of()
                );

        assertThatThrownBy(
                () -> registry.require("SMS")
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "No external provider adapter registered for SMS"
                );
    }

    @Test
    void rejectsDuplicateNormalizedConnectorType() {
        ExternalProviderAdapter first =
                adapter("WHATSAPP");

        ExternalProviderAdapter second =
                adapter(" whatsapp ");

        assertThatThrownBy(
                () -> new ExternalProviderAdapterRegistry(
                        List.of(
                                first,
                                second
                        )
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "Multiple external provider adapters registered for WHATSAPP"
                );
    }

    @Test
    void rejectsNullAdapter() {
        List<ExternalProviderAdapter> adapters =
                Arrays.asList(
                        (ExternalProviderAdapter) null
                );

        assertThatThrownBy(
                () -> new ExternalProviderAdapterRegistry(
                        adapters
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "External provider adapter must not be null"
                );
    }

    @Test
    void rejectsBlankConnectorType() {
        ExternalProviderAdapter adapter =
                adapter("   ");

        assertThatThrownBy(
                () -> new ExternalProviderAdapterRegistry(
                        List.of(adapter)
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "connectorType must not be blank"
                );
    }

    private ExternalProviderAdapter adapter(
            String connectorType
    ) {
        ExternalProviderAdapter adapter =
                mock(ExternalProviderAdapter.class);

        when(adapter.connectorType())
                .thenReturn(connectorType);

        return adapter;
    }
}
