package africa.growtogether.platform.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConfigurationDomainTest {

    @Test
    void normalizesDefinitionAndValidatesInteger() {
        ConfigurationDefinition definition = new ConfigurationDefinition(
            " school.academic-year ",
            "Academic year",
            "academic",
            null,
            ConfigurationDataType.INTEGER,
            "2026",
            "{}",
            Set.of(ConfigurationScope.TENANT),
            true,
            false
        );

        assertEquals(
            "SCHOOL.ACADEMIC-YEAR",
            definition.getCode()
        );

        String validatedValue = ConfigurationValidator.validate(
            definition,
            "2027"
        );

        ConfigurationValue value = new ConfigurationValue(
            definition,
            ConfigurationScope.TENANT,
            null,
            null,
            UUID.randomUUID()
        );

        value.writePlain(
            validatedValue,
            "change",
            "test-hash"
        );

        assertEquals(
            "2027",
            value.getStoredValue()
        );
    }

    @Test
    void rejectsDisallowedScope() {
        ConfigurationDefinition definition = new ConfigurationDefinition(
            "PAYMENTS.CURRENCY",
            "Currency",
            "payments",
            null,
            ConfigurationDataType.STRING,
            "UGX",
            "{}",
            Set.of(ConfigurationScope.PLATFORM),
            true,
            false
        );

        assertThrows(
            ConfigurationException.class,
            () -> new ConfigurationValue(
                definition,
                ConfigurationScope.TENANT,
                null,
                null,
                UUID.randomUUID()
            )
        );
    }

    @Test
    void rejectsInvalidBoolean() {
        ConfigurationDefinition definition = new ConfigurationDefinition(
            "FEATURE.ENABLED",
            "Enabled",
            "features",
            null,
            ConfigurationDataType.BOOLEAN,
            "false",
            "{}",
            Set.of(ConfigurationScope.PLATFORM),
            true,
            false
        );

        assertThrows(
            ConfigurationException.class,
            () -> ConfigurationValidator.validate(
                definition,
                "yes"
            )
        );
    }
}