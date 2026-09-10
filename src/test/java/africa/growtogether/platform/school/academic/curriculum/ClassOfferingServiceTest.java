package africa.growtogether.platform.school.academic.curriculum;

import africa.growtogether.platform.school.academic.year.AcademicYearService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ClassOfferingServiceTest {


    @Mock
    private ClassOfferingRepository repository;

    @Mock
    private AcademicYearService academicYears;

    @Mock
    private CampusService campuses;

    @Mock
    private ClassGradeService classGrades;


    private ClassOfferingService service;


    @BeforeEach
    void setUp() {

        service =
                new ClassOfferingService(
                        repository,
                        academicYears,
                        campuses,
                        classGrades
                );

    }


    @Test
    void createRequiresAllRequiredParentsFromSameTenant() {

        UUID tenantId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();


        when(
                repository.save(
                        any(ClassOffering.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );


        service.create(
                tenantId,
                "2027-MAIN-P1",
                academicYearId,
                campusId,
                null,
                null,
                null,
                classGradeId,
                40,
                10,
                40,
                null,
                null
        );


        verify(academicYears).get(
                tenantId,
                academicYearId
        );

        verify(campuses).get(
                tenantId,
                campusId
        );

        verify(classGrades).get(
                tenantId,
                classGradeId
        );

        verify(repository).save(
                any(ClassOffering.class)
        );

    }


    @Test
    void wrongTenantAcademicYearPreventsSave() {

        UUID tenantId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();


        when(
                academicYears.get(
                        tenantId,
                        academicYearId
                )
        ).thenThrow(
                new IllegalArgumentException(
                        "Academic year not found for tenant"
                )
        );


        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(
                        tenantId,
                        "2027-MAIN-P1",
                        academicYearId,
                        UUID.randomUUID(),
                        null,
                        null,
                        null,
                        UUID.randomUUID(),
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );


        verify(
                repository,
                never()
        ).save(
                any(ClassOffering.class)
        );

    }


    @Test
    void wrongTenantCampusPreventsSave() {

        UUID tenantId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();


        when(
                campuses.get(
                        tenantId,
                        campusId
                )
        ).thenThrow(
                new IllegalArgumentException(
                        "Campus not found for tenant"
                )
        );


        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(
                        tenantId,
                        "2027-MAIN-P1",
                        UUID.randomUUID(),
                        campusId,
                        null,
                        null,
                        null,
                        UUID.randomUUID(),
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );


        verify(
                repository,
                never()
        ).save(
                any(ClassOffering.class)
        );

    }


    @Test
    void wrongTenantClassGradePreventsSave() {

        UUID tenantId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();


        when(
                classGrades.get(
                        tenantId,
                        classGradeId
                )
        ).thenThrow(
                new IllegalArgumentException(
                        "Class grade not found for tenant"
                )
        );


        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(
                        tenantId,
                        "2027-MAIN-P1",
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        null,
                        null,
                        classGradeId,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );


        verify(
                repository,
                never()
        ).save(
                any(ClassOffering.class)
        );

    }

}
