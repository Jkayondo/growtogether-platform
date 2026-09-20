package africa.growtogether.platform.school.teacher.programme;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.connect.ConnectTeacherAuthorizationService;
import africa.growtogether.platform.school.profile.SchoolProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class TeacherProgrammeDayService {
    private final EnterpriseIdentityContext identity;
    private final ConnectTeacherAuthorizationService teachers;
    private final SchoolProfileService schools;
    private final Clock clock;

    @Autowired
    public TeacherProgrammeDayService(
            EnterpriseIdentityContext identity,
            ConnectTeacherAuthorizationService teachers,
            SchoolProfileService schools) {
        this(identity, teachers, schools, Clock.systemUTC());
    }

    TeacherProgrammeDayService(
            EnterpriseIdentityContext identity,
            ConnectTeacherAuthorizationService teachers,
            SchoolProfileService schools,
            Clock clock) {
        this.identity = identity;
        this.teachers = teachers;
        this.schools = schools;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ProgrammeDay currentDay() {
        UUID tenantId = identity.requireTenantId();
        UUID teacherId = teachers.requireUniqueCurrentTeacherProfile().getId();
        ZoneId zone = schools.requireTimezone(tenantId);
        LocalDate date = LocalDate.now(clock.withZone(zone));
        return new ProgrammeDay(
                tenantId,
                teacherId,
                date,
                zone,
                date.atStartOfDay(zone).toInstant(),
                date.plusDays(1).atStartOfDay(zone).toInstant()
        );
    }

    public record ProgrammeDay(
            UUID tenantId,
            UUID teacherProfileId,
            LocalDate date,
            ZoneId zone,
            Instant startInclusive,
            Instant endExclusive) {}
}
