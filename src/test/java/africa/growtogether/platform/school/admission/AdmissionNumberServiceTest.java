package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.school.academic.year.AcademicYear;
import africa.growtogether.platform.school.academic.year.AcademicYearRepository;
import africa.growtogether.platform.school.profile.SchoolProfile;
import africa.growtogether.platform.school.profile.SchoolProfileService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdmissionNumberServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SchoolProfileService schoolProfiles;

    @Mock
    private AcademicYearRepository academicYears;

    private AdmissionNumberService service;

    @BeforeEach
    void setUp() {

        service =
                new AdmissionNumberService(
                        jdbcTemplate,
                        schoolProfiles,
                        academicYears
                );
    }

    @Test
    void generatesAdmissionNumberFromSchoolCodeAcademicYearAndSequence() {

        UUID tenantId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        SchoolProfile schoolProfile =
                new SchoolProfile(
                        "PPIS",
                        "Pio and Pretty International School",
                        null,
                        null,
                        "UG",
                        "UGX",
                        "Africa/Kampala",
                        null,
                        null,
                        null
                );

        AcademicYear academicYear =
                new AcademicYear(
                        tenantId,
                        "2026",
                        "Academic Year 2026",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31)
                );

        when(
                schoolProfiles.getForTenant(
                        tenantId
                )
        ).thenReturn(
                schoolProfile
        );

        when(
                academicYears.findByTenantIdAndId(
                        tenantId,
                        academicYearId
                )
        ).thenReturn(
                Optional.of(
                        academicYear
                )
        );

        when(
                jdbcTemplate.queryForObject(
                        anyString(),
                        eq(Long.class),
                        eq(tenantId),
                        eq(academicYearId)
                )
        ).thenReturn(
                1L
        );

        String number =
                service.next(
                        tenantId,
                        academicYearId
                );

        assertEquals(
                "PPIS-2026-ADM-000001",
                number
        );
    }

    @Test
    void usesAcademicYearStartYearRatherThanCurrentCalendarYear() {

        UUID tenantId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        SchoolProfile schoolProfile =
                new SchoolProfile(
                        "ABC",
                        "Example School",
                        null,
                        null,
                        "UG",
                        "UGX",
                        "Africa/Kampala",
                        null,
                        null,
                        null
                );

        AcademicYear academicYear =
                new AcademicYear(
                        tenantId,
                        "2026/2027",
                        "Academic Year 2026/2027",
                        LocalDate.of(2026, 8, 15),
                        LocalDate.of(2027, 6, 30)
                );

        when(
                schoolProfiles.getForTenant(
                        tenantId
                )
        ).thenReturn(
                schoolProfile
        );

        when(
                academicYears.findByTenantIdAndId(
                        tenantId,
                        academicYearId
                )
        ).thenReturn(
                Optional.of(
                        academicYear
                )
        );

        when(
                jdbcTemplate.queryForObject(
                        anyString(),
                        eq(Long.class),
                        eq(tenantId),
                        eq(academicYearId)
                )
        ).thenReturn(
                42L
        );

        assertEquals(
                "ABC-2026-ADM-000042",
                service.next(
                        tenantId,
                        academicYearId
                )
        );
    }

    @Test
    void normalizesSchoolCodeForSafeReferenceUse() {

        UUID tenantId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        SchoolProfile schoolProfile =
                new SchoolProfile(
                        "Pio & Pretty",
                        "Pio and Pretty International School",
                        null,
                        null,
                        "UG",
                        "UGX",
                        "Africa/Kampala",
                        null,
                        null,
                        null
                );

        AcademicYear academicYear =
                new AcademicYear(
                        tenantId,
                        "2026",
                        "2026",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31)
                );

        when(
                schoolProfiles.getForTenant(
                        tenantId
                )
        ).thenReturn(
                schoolProfile
        );

        when(
                academicYears.findByTenantIdAndId(
                        tenantId,
                        academicYearId
                )
        ).thenReturn(
                Optional.of(
                        academicYear
                )
        );

        when(
                jdbcTemplate.queryForObject(
                        anyString(),
                        eq(Long.class),
                        eq(tenantId),
                        eq(academicYearId)
                )
        ).thenReturn(
                7L
        );

        assertEquals(
                "PIO-PRETTY-2026-ADM-000007",
                service.next(
                        tenantId,
                        academicYearId
                )
        );
    }

    @Test
    void rejectsMissingTenant() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.next(
                                null,
                                UUID.randomUUID()
                        )
        );

        verifyNoInteractions(
                jdbcTemplate
        );
    }

    @Test
    void rejectsMissingAcademicYear() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.next(
                                UUID.randomUUID(),
                                null
                        )
        );

        verifyNoInteractions(
                jdbcTemplate
        );
    }

    @Test
    void rejectsAcademicYearOutsideTenant() {

        UUID tenantId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        SchoolProfile schoolProfile =
                new SchoolProfile(
                        "PPIS",
                        "Pio and Pretty International School",
                        null,
                        null,
                        "UG",
                        "UGX",
                        "Africa/Kampala",
                        null,
                        null,
                        null
                );

        when(
                schoolProfiles.getForTenant(
                        tenantId
                )
        ).thenReturn(
                schoolProfile
        );

        when(
                academicYears.findByTenantIdAndId(
                        tenantId,
                        academicYearId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.next(
                                        tenantId,
                                        academicYearId
                                )
                );

        assertEquals(
                "Academic year not found for tenant",
                exception.getMessage()
        );

        verifyNoInteractions(
                jdbcTemplate
        );
    }
}
