package africa.growtogether.platform.school.guardian;

import africa.growtogether.platform.school.profile.SchoolProfile;
import africa.growtogether.platform.school.profile.SchoolProfileService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuardianNumberServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SchoolProfileService schoolProfiles;

    private GuardianNumberService service;

    @BeforeEach
    void setUp() {

        service =
                new GuardianNumberService(
                        jdbcTemplate,
                        schoolProfiles
                );
    }

    @Test
    void generatesPermanentGuardianNumberFromSchoolCodeAndSequence() {

        UUID tenantId =
                UUID.randomUUID();

        SchoolProfile schoolProfile =
                schoolProfile(
                        "PPIS"
                );

        when(
                schoolProfiles.getForTenant(
                        tenantId
                )
        ).thenReturn(
                schoolProfile
        );

        when(
                jdbcTemplate.queryForObject(
                        anyString(),
                        eq(Long.class),
                        eq(tenantId)
                )
        ).thenReturn(
                1L
        );

        assertEquals(
                "PPIS-GDN-000001",
                service.next(
                        tenantId
                )
        );
    }

    @Test
    void normalizesSchoolCodeForSafeGuardianReference() {

        UUID tenantId =
                UUID.randomUUID();

        SchoolProfile schoolProfile =
                schoolProfile(
                        "Pio & Pretty"
                );

        when(
                schoolProfiles.getForTenant(
                        tenantId
                )
        ).thenReturn(
                schoolProfile
        );

        when(
                jdbcTemplate.queryForObject(
                        anyString(),
                        eq(Long.class),
                        eq(tenantId)
                )
        ).thenReturn(
                42L
        );

        assertEquals(
                "PIO-PRETTY-GDN-000042",
                service.next(
                        tenantId
                )
        );
    }

    @Test
    void rejectsMissingTenant() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.next(
                        null
                )
        );

        verifyNoInteractions(
                jdbcTemplate,
                schoolProfiles
        );
    }

    @Test
    void rejectsInvalidSchoolCodeBeforeIssuingSequence() {

        UUID tenantId =
                UUID.randomUUID();

        SchoolProfile schoolProfile =
                schoolProfile(
                        "!!!"
                );

        when(
                schoolProfiles.getForTenant(
                        tenantId
                )
        ).thenReturn(
                schoolProfile
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.next(
                                tenantId
                        )
                );

        assertEquals(
                "School code is invalid for guardian numbering",
                exception.getMessage()
        );

        verifyNoInteractions(
                jdbcTemplate
        );
    }

    @Test
    void rejectsInvalidReturnedSequence() {

        UUID tenantId =
                UUID.randomUUID();

        SchoolProfile schoolProfile =
                schoolProfile(
                        "PPIS"
                );

        when(
                schoolProfiles.getForTenant(
                        tenantId
                )
        ).thenReturn(
                schoolProfile
        );

        when(
                jdbcTemplate.queryForObject(
                        anyString(),
                        eq(Long.class),
                        eq(tenantId)
                )
        ).thenReturn(
                0L
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.next(
                                tenantId
                        )
                );

        assertEquals(
                "Unable to generate guardian sequence",
                exception.getMessage()
        );
    }

    private static SchoolProfile schoolProfile(
            String schoolCode
    ) {

        return new SchoolProfile(
                schoolCode,
                "Test School",
                null,
                null,
                "UG",
                "UGX",
                "Africa/Kampala",
                null,
                null,
                null
        );
    }
}
