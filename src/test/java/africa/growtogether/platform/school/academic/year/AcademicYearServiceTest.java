package africa.growtogether.platform.school.academic.year;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class AcademicYearServiceTest {

    private AcademicYearRepository repository;

    private AcademicYearService service;


    @BeforeEach
    void setUp() {

        repository =
                mock(
                        AcademicYearRepository.class
                );

        service =
                new AcademicYearService(
                        repository
                );
    }


    @Test
    void createPersistsAcademicYearForRequestedTenant() {

        UUID tenantId =
                UUID.randomUUID();

        CreateAcademicYearCommand command =
                new CreateAcademicYearCommand(
                        "2027",
                        "Academic Year 2027",
                        LocalDate.of(
                                2027,
                                1,
                                1
                        ),
                        LocalDate.of(
                                2027,
                                12,
                                31
                        )
                );

        when(
                repository.save(
                        any(
                                AcademicYear.class
                        )
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(
                                0
                        )
        );

        AcademicYear result =
                service.create(
                        tenantId,
                        command
                );

        ArgumentCaptor<AcademicYear> captor =
                ArgumentCaptor.forClass(
                        AcademicYear.class
                );

        verify(
                repository
        ).save(
                captor.capture()
        );

        AcademicYear saved =
                captor.getValue();

        assertEquals(
                tenantId,
                saved.getTenantId()
        );

        assertEquals(
                "2027",
                saved.getAcademicYearCode()
        );

        assertEquals(
                "Academic Year 2027",
                saved.getAcademicYearName()
        );

        assertEquals(
                LocalDate.of(
                        2027,
                        1,
                        1
                ),
                saved.getStartDate()
        );

        assertEquals(
                LocalDate.of(
                        2027,
                        12,
                        31
                ),
                saved.getEndDate()
        );

        assertFalse(
                saved.isCurrentYear()
        );

        assertEquals(
                saved,
                result
        );
    }


    @Test
    void createRejectsEqualStartAndEndDates() {

        UUID tenantId =
                UUID.randomUUID();

        LocalDate date =
                LocalDate.of(
                        2027,
                        1,
                        1
                );

        CreateAcademicYearCommand command =
                new CreateAcademicYearCommand(
                        "2027",
                        "Academic Year 2027",
                        date,
                        date
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.create(
                                        tenantId,
                                        command
                                )
                );

        assertEquals(
                "Academic year end date must be after start date",
                exception.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any()
        );
    }


    @Test
    void createRejectsEndDateBeforeStartDate() {

        UUID tenantId =
                UUID.randomUUID();

        CreateAcademicYearCommand command =
                new CreateAcademicYearCommand(
                        "2027",
                        "Academic Year 2027",
                        LocalDate.of(
                                2027,
                                12,
                                31
                        ),
                        LocalDate.of(
                                2027,
                                1,
                                1
                        )
                );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.create(
                                tenantId,
                                command
                        )
        );

        verify(
                repository,
                never()
        ).save(
                any()
        );
    }


    @Test
    void findByTenantUsesTenantScopedRepositoryQuery() {

        UUID tenantId =
                UUID.randomUUID();

        List<AcademicYear> expected =
                List.of(
                        new AcademicYear(
                                tenantId,
                                "2027",
                                "Academic Year 2027",
                                LocalDate.of(
                                        2027,
                                        1,
                                        1
                                ),
                                LocalDate.of(
                                        2027,
                                        12,
                                        31
                                )
                        )
                );

        when(
                repository.findByTenantId(
                        tenantId
                )
        ).thenReturn(
                expected
        );

        List<AcademicYear> result =
                service.findByTenant(
                        tenantId
                );

        assertEquals(
                expected,
                result
        );

        verify(
                repository
        ).findByTenantId(
                tenantId
        );
    }
}
