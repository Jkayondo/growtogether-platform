package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.school.academic.curriculum.Campus;
import africa.growtogether.platform.school.academic.curriculum.CampusRepository;
import africa.growtogether.platform.school.academic.curriculum.ClassGrade;
import africa.growtogether.platform.school.academic.curriculum.ClassGradeRepository;
import africa.growtogether.platform.school.academic.curriculum.Stream;
import africa.growtogether.platform.school.academic.curriculum.StreamRepository;
import africa.growtogether.platform.school.academic.year.AcademicYear;
import africa.growtogether.platform.school.academic.year.AcademicYearRepository;
import africa.growtogether.platform.school.profile.SchoolProfileService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdmissionApplicationServiceTest {

    @Mock
    private AdmissionApplicationRepository repository;

    @Mock
    private AdmissionNumberService admissionNumbers;

    @Mock
    private AcademicYearRepository academicYears;

    @Mock
    private CampusRepository campuses;

    @Mock
    private ClassGradeRepository classGrades;

    @Mock
    private StreamRepository streams;

    @Mock
    private SchoolProfileService schoolProfiles;

    @Mock
    private AcademicYear academicYear;

    @Mock
    private Campus campus;

    @Mock
    private ClassGrade classGrade;

    @Mock
    private Stream stream;

    private AdmissionApplicationService service;

    @BeforeEach
    void setUp() {

        service =
                new AdmissionApplicationService(
                        repository,
                        admissionNumbers,
                        academicYears,
                        campuses,
                        classGrades,
                        streams,
                        schoolProfiles
                );
    }

    @Test
    void createsTenantScopedDraftWithGeneratedApplicationNumber() {

        UUID tenantId = UUID.randomUUID();
        UUID academicYearId = UUID.randomUUID();
        UUID campusId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();

        CreateAdmissionApplicationCommand command =
                new CreateAdmissionApplicationCommand(
                        academicYearId,
                        campusId,
                        classGradeId,
                        null,
                        "OFFICE"
                );

        when(
                academicYears.findByTenantIdAndId(
                        tenantId,
                        academicYearId
                )
        ).thenReturn(
                Optional.of(academicYear)
        );

        when(
                campuses.findByTenantIdAndId(
                        tenantId,
                        campusId
                )
        ).thenReturn(
                Optional.of(campus)
        );

        when(
                classGrades.findByTenantIdAndId(
                        tenantId,
                        classGradeId
                )
        ).thenReturn(
                Optional.of(classGrade)
        );

        when(
                schoolProfiles.requireTimezone(
                        tenantId
                )
        ).thenReturn(
                ZoneId.of("Africa/Kampala")
        );

        when(
                admissionNumbers.next(
                        tenantId,
                        academicYearId
                )
        ).thenReturn(
                "PPIS-2026-ADM-000001"
        );

        when(
                repository.save(
                        any(AdmissionApplication.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        AdmissionApplication result =
                service.createDraft(
                        tenantId,
                        command
                );

        assertEquals(
                tenantId,
                result.getTenantId()
        );

        assertEquals(
                "PPIS-2026-ADM-000001",
                result.getApplicationNumber()
        );

        assertEquals(
                academicYearId,
                result.getAcademicYearId()
        );

        assertEquals(
                campusId,
                result.getCampusId()
        );

        assertEquals(
                classGradeId,
                result.getDesiredClassGradeId()
        );

        assertNull(
                result.getDesiredStreamId()
        );

        assertEquals(
                "DRAFT",
                result.getAdmissionStatus()
        );

        assertEquals(
                "OFFICE",
                result.getSubmissionChannel()
        );

        assertEquals(
                LocalDate.now(
                        ZoneId.of("Africa/Kampala")
                ),
                result.getApplicationDate()
        );

        verify(
                admissionNumbers
        ).next(
                tenantId,
                academicYearId
        );

        verify(
                repository
        ).save(
                result
        );
    }

    @Test
    void validatesSelectedStreamAgainstCampusAndClassGrade() {

        UUID tenantId = UUID.randomUUID();
        UUID academicYearId = UUID.randomUUID();
        UUID campusId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();
        UUID streamId = UUID.randomUUID();

        CreateAdmissionApplicationCommand command =
                new CreateAdmissionApplicationCommand(
                        academicYearId,
                        campusId,
                        classGradeId,
                        streamId,
                        "MOBILE"
                );

        when(
                academicYears.findByTenantIdAndId(
                        tenantId,
                        academicYearId
                )
        ).thenReturn(
                Optional.of(academicYear)
        );

        when(
                campuses.findByTenantIdAndId(
                        tenantId,
                        campusId
                )
        ).thenReturn(
                Optional.of(campus)
        );

        when(
                classGrades.findByTenantIdAndId(
                        tenantId,
                        classGradeId
                )
        ).thenReturn(
                Optional.of(classGrade)
        );

        when(
                streams.findByTenantIdAndId(
                        tenantId,
                        streamId
                )
        ).thenReturn(
                Optional.of(stream)
        );

        when(
                stream.getCampusId()
        ).thenReturn(
                campusId
        );

        when(
                stream.getClassGradeId()
        ).thenReturn(
                classGradeId
        );

        when(
                schoolProfiles.requireTimezone(
                        tenantId
                )
        ).thenReturn(
                ZoneId.of("Africa/Kampala")
        );

        when(
                admissionNumbers.next(
                        tenantId,
                        academicYearId
                )
        ).thenReturn(
                "PPIS-2026-ADM-000002"
        );

        when(
                repository.save(
                        any(AdmissionApplication.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        AdmissionApplication result =
                service.createDraft(
                        tenantId,
                        command
                );

        assertEquals(
                streamId,
                result.getDesiredStreamId()
        );

        assertEquals(
                "MOBILE",
                result.getSubmissionChannel()
        );
    }

    @Test
    void rejectsStreamOutsideTenant() {

        UUID tenantId = UUID.randomUUID();
        UUID academicYearId = UUID.randomUUID();
        UUID campusId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();
        UUID streamId = UUID.randomUUID();

        CreateAdmissionApplicationCommand command =
                new CreateAdmissionApplicationCommand(
                        academicYearId,
                        campusId,
                        classGradeId,
                        streamId,
                        "ONLINE"
                );

        prepareScope(
                tenantId,
                academicYearId,
                campusId,
                classGradeId
        );

        when(
                streams.findByTenantIdAndId(
                        tenantId,
                        streamId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.createDraft(
                                        tenantId,
                                        command
                                )
                );

        assertEquals(
                "Stream not found for tenant",
                exception.getMessage()
        );

        verifyNoInteractions(
                admissionNumbers
        );

        verify(
                repository,
                never()
        ).save(
                any()
        );
    }

    @Test
    void rejectsStreamBelongingToDifferentCampus() {

        UUID tenantId = UUID.randomUUID();
        UUID academicYearId = UUID.randomUUID();
        UUID campusId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();
        UUID streamId = UUID.randomUUID();

        CreateAdmissionApplicationCommand command =
                new CreateAdmissionApplicationCommand(
                        academicYearId,
                        campusId,
                        classGradeId,
                        streamId,
                        "ONLINE"
                );

        prepareScope(
                tenantId,
                academicYearId,
                campusId,
                classGradeId
        );

        when(
                streams.findByTenantIdAndId(
                        tenantId,
                        streamId
                )
        ).thenReturn(
                Optional.of(stream)
        );

        when(
                stream.getCampusId()
        ).thenReturn(
                UUID.randomUUID()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.createDraft(
                                        tenantId,
                                        command
                                )
                );

        assertEquals(
                "Stream does not belong to campus",
                exception.getMessage()
        );

        verifyNoInteractions(
                admissionNumbers
        );

        verify(
                repository,
                never()
        ).save(
                any()
        );
    }

    @Test
    void rejectsStreamBelongingToDifferentClassGrade() {

        UUID tenantId = UUID.randomUUID();
        UUID academicYearId = UUID.randomUUID();
        UUID campusId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();
        UUID streamId = UUID.randomUUID();

        CreateAdmissionApplicationCommand command =
                new CreateAdmissionApplicationCommand(
                        academicYearId,
                        campusId,
                        classGradeId,
                        streamId,
                        "ONLINE"
                );

        prepareScope(
                tenantId,
                academicYearId,
                campusId,
                classGradeId
        );

        when(
                streams.findByTenantIdAndId(
                        tenantId,
                        streamId
                )
        ).thenReturn(
                Optional.of(stream)
        );

        when(
                stream.getCampusId()
        ).thenReturn(
                campusId
        );

        when(
                stream.getClassGradeId()
        ).thenReturn(
                UUID.randomUUID()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.createDraft(
                                        tenantId,
                                        command
                                )
                );

        assertEquals(
                "Stream does not belong to class grade",
                exception.getMessage()
        );

        verifyNoInteractions(
                admissionNumbers
        );

        verify(
                repository,
                never()
        ).save(
                any()
        );
    }

    @Test
    void rejectsAcademicYearOutsideTenant() {

        UUID tenantId = UUID.randomUUID();
        UUID academicYearId = UUID.randomUUID();

        CreateAdmissionApplicationCommand command =
                new CreateAdmissionApplicationCommand(
                        academicYearId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        "ONLINE"
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
                                service.createDraft(
                                        tenantId,
                                        command
                                )
                );

        assertEquals(
                "Academic year not found for tenant",
                exception.getMessage()
        );

        verifyNoInteractions(
                admissionNumbers
        );
    }

    @Test
    void rejectsCampusOutsideTenant() {

        UUID tenantId = UUID.randomUUID();
        UUID academicYearId = UUID.randomUUID();
        UUID campusId = UUID.randomUUID();

        CreateAdmissionApplicationCommand command =
                new CreateAdmissionApplicationCommand(
                        academicYearId,
                        campusId,
                        UUID.randomUUID(),
                        null,
                        "ONLINE"
                );

        when(
                academicYears.findByTenantIdAndId(
                        tenantId,
                        academicYearId
                )
        ).thenReturn(
                Optional.of(academicYear)
        );

        when(
                campuses.findByTenantIdAndId(
                        tenantId,
                        campusId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.createDraft(
                                        tenantId,
                                        command
                                )
                );

        assertEquals(
                "Campus not found for tenant",
                exception.getMessage()
        );

        verifyNoInteractions(
                admissionNumbers
        );
    }

    @Test
    void rejectsClassGradeOutsideTenant() {

        UUID tenantId = UUID.randomUUID();
        UUID academicYearId = UUID.randomUUID();
        UUID campusId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();

        CreateAdmissionApplicationCommand command =
                new CreateAdmissionApplicationCommand(
                        academicYearId,
                        campusId,
                        classGradeId,
                        null,
                        "ONLINE"
                );

        when(
                academicYears.findByTenantIdAndId(
                        tenantId,
                        academicYearId
                )
        ).thenReturn(
                Optional.of(academicYear)
        );

        when(
                campuses.findByTenantIdAndId(
                        tenantId,
                        campusId
                )
        ).thenReturn(
                Optional.of(campus)
        );

        when(
                classGrades.findByTenantIdAndId(
                        tenantId,
                        classGradeId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.createDraft(
                                        tenantId,
                                        command
                                )
                );

        assertEquals(
                "Class grade not found for tenant",
                exception.getMessage()
        );

        verifyNoInteractions(
                admissionNumbers
        );
    }

    @Test
    void rejectsMissingTenantBeforeRepositoryAccess() {

        CreateAdmissionApplicationCommand command =
                new CreateAdmissionApplicationCommand(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        "ONLINE"
                );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.createDraft(
                                null,
                                command
                        )
        );

        verifyNoInteractions(
                academicYears,
                campuses,
                classGrades,
                streams,
                schoolProfiles,
                admissionNumbers,
                repository
        );
    }

    @Test
    void rejectsMissingCommandBeforeRepositoryAccess() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.createDraft(
                                UUID.randomUUID(),
                                null
                        )
        );

        verifyNoInteractions(
                academicYears,
                campuses,
                classGrades,
                streams,
                schoolProfiles,
                admissionNumbers,
                repository
        );
    }

    private void prepareScope(
            UUID tenantId,
            UUID academicYearId,
            UUID campusId,
            UUID classGradeId
    ) {

        when(
                academicYears.findByTenantIdAndId(
                        tenantId,
                        academicYearId
                )
        ).thenReturn(
                Optional.of(academicYear)
        );

        when(
                campuses.findByTenantIdAndId(
                        tenantId,
                        campusId
                )
        ).thenReturn(
                Optional.of(campus)
        );

        when(
                classGrades.findByTenantIdAndId(
                        tenantId,
                        classGradeId
                )
        ).thenReturn(
                Optional.of(classGrade)
        );
    }
}
