package africa.growtogether.platform.system;

import java.time.Instant;

public record SystemStatus(String service, String status, Instant timestamp) {}
