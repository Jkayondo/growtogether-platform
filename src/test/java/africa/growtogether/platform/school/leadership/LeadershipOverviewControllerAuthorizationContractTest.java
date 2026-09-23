package africa.growtogether.platform.school.leadership;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LeadershipOverviewControllerAuthorizationContractTest {

    @Test
    void overviewRequiresDedicatedLeadershipReadAuthority()
            throws Exception {

        var method =
                LeadershipOverviewController.class.getMethod(
                        "overview",
                        UUID.class
                );

        PreAuthorize preAuthorize =
                method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);

        assertEquals(
                "hasAuthority('school.leadership.overview.read')",
                preAuthorize.value()
        );
    }
}
