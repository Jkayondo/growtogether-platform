package africa.growtogether.platform.eip;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
class ExternalProviderAdapterRegistry {

    private final Map<String, ExternalProviderAdapter> adapters;

    ExternalProviderAdapterRegistry(
            List<ExternalProviderAdapter> adapters
    ) {
        Map<String, ExternalProviderAdapter> indexed =
                new HashMap<>();

        for (ExternalProviderAdapter adapter : adapters) {
            if (adapter == null) {
                throw new IllegalStateException(
                        "External provider adapter must not be null"
                );
            }

            String connectorType =
                    normalize(
                            adapter.connectorType()
                    );

            ExternalProviderAdapter previous =
                    indexed.putIfAbsent(
                            connectorType,
                            adapter
                    );

            if (previous != null) {
                throw new IllegalStateException(
                        "Multiple external provider adapters registered for "
                                + connectorType
                );
            }
        }

        this.adapters =
                Map.copyOf(indexed);
    }

    ExternalProviderAdapter require(
            String connectorType
    ) {
        String normalized =
                normalize(connectorType);

        ExternalProviderAdapter adapter =
                adapters.get(normalized);

        if (adapter == null) {
            throw new IllegalStateException(
                    "No external provider adapter registered for "
                            + normalized
            );
        }

        return adapter;
    }

    private static String normalize(
            String connectorType
    ) {
        if (
                connectorType == null
                || connectorType.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "connectorType must not be blank"
            );
        }

        return connectorType
                .trim()
                .toUpperCase(Locale.ROOT);
    }
}
