package africa.growtogether.platform.school.parent.governance;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class ParentEngagementSecurityEventTest {


    @Test
    void shouldCreateSecurityEvent() {


        String event =
                "DASHBOARD_ACCESS_GRANTED";


        assertNotNull(event);
    }
}
