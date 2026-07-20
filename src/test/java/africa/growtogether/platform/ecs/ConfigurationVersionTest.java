package africa.growtogether.platform.ecs;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ConfigurationVersionTest {
    @Test void secretDefinitionIsRecognizedAcrossProducts() {
        var definition = new ConfigurationDefinition("payments.api-key","API key","PAYMENTS",null,
            ConfigurationDataType.SECRET,null,"{}",Set.of(ConfigurationScope.PLATFORM,ConfigurationScope.TENANT),true,true);
        assertTrue(definition.isSecret());
        assertTrue(definition.allows(ConfigurationScope.TENANT));
    }
}
