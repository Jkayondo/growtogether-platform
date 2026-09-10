package africa.growtogether.platform.school.assessment.marking;


import africa.growtogether.platform.school.academic.curriculum.ClassOffering;
import africa.growtogether.platform.school.academic.curriculum.ClassOfferingRepository;
import africa.growtogether.platform.school.academic.curriculum.StreamRepository;
import africa.growtogether.platform.school.academic.curriculum.SubjectOffering;
import africa.growtogether.platform.school.academic.curriculum.SubjectOfferingRepository;
import africa.growtogether.platform.school.academic.teaching.TeacherProfileRepository;
import africa.growtogether.platform.school.assessment.examination.paper.AssessmentPaperRepository;
import africa.growtogether.platform.school.assessment.examination.schedule.ExaminationScheduleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class MarkSheetServiceTest {


    private MarkSheetRepository repository;

    private AssessmentComponentReferenceGateway
            assessmentComponentGateway;

    private AssessmentPaperRepository
            assessmentPaperRepository;

    private ExaminationScheduleRepository
            examinationScheduleRepository;

    private SubjectOfferingRepository
            subjectOfferingRepository;

    private ClassOfferingRepository
            classOfferingRepository;

    private StreamRepository
            streamRepository;

    private TeacherProfileRepository
            teacherProfileRepository;

    private MarkSheetService service;


    private UUID tenantId;

    private UUID componentId;

    private UUID subjectOfferingId;

    private UUID classOfferingId;


    @BeforeEach
    void setUp() {

        repository =
                mock(
                        MarkSheetRepository.class
                );

        assessmentComponentGateway =
                mock(
                        AssessmentComponentReferenceGateway.class
                );

        assessmentPaperRepository =
                mock(
                        AssessmentPaperRepository.class
                );

        examinationScheduleRepository =
                mock(
                        ExaminationScheduleRepository.class
                );

        subjectOfferingRepository =
                mock(
                        SubjectOfferingRepository.class
                );

        classOfferingRepository =
                mock(
                        ClassOfferingRepository.class
                );

        streamRepository =
                mock(
                        StreamRepository.class
                );

        teacherProfileRepository =
                mock(
                        TeacherProfileRepository.class
                );


        service =
                new MarkSheetService(
                        repository,
                        assessmentComponentGateway,
                        assessmentPaperRepository,
                        examinationScheduleRepository,
                        subjectOfferingRepository,
                        classOfferingRepository,
                        streamRepository,
                        teacherProfileRepository
                );


        tenantId =
                UUID.randomUUID();

        componentId =
                UUID.randomUUID();

        subjectOfferingId =
                UUID.randomUUID();

        classOfferingId =
                UUID.randomUUID();
    }


    private CreateMarkSheetCommand command() {

        return new CreateMarkSheetCommand(
                "MS-2026-001",
                componentId,
                null,
                null,
                subjectOfferingId,
                classOfferingId,
                null,
                null,
                new BigDecimal("100.00"),
                new BigDecimal("50.00")
        );
    }


    private void arrangeValidParents() {

        when(
                repository
                        .existsByTenantIdAndMarkSheetReference(
                                tenantId,
                                "MS-2026-001"
                        )
        )
                .thenReturn(
                        false
                );


        when(
                assessmentComponentGateway.find(
                        tenantId,
                        componentId
                )
        )
                .thenReturn(
                        Optional.of(
                                new AssessmentComponentReferenceGateway
                                        .AssessmentComponentReference(
                                                componentId,
                                                UUID.randomUUID(),
                                                UUID.randomUUID(),
                                                subjectOfferingId,
                                                new BigDecimal("100.00"),
                                                new BigDecimal("50.00"),
                                                "PLANNED",
                                                "ACTIVE"
                                        )
                        )
                );


        SubjectOffering subjectOffering =
                mock(
                        SubjectOffering.class
                );

        when(
                subjectOffering.getId()
        )
                .thenReturn(
                        subjectOfferingId
                );

        when(
                subjectOffering.getClassOfferingId()
        )
                .thenReturn(
                        classOfferingId
                );

        when(
                subjectOffering.getStreamId()
        )
                .thenReturn(
                        null
                );


        when(
                subjectOfferingRepository.findByTenantIdAndId(
                        tenantId,
                        subjectOfferingId
                )
        )
                .thenReturn(
                        Optional.of(
                                subjectOffering
                        )
                );


        ClassOffering classOffering =
                mock(
                        ClassOffering.class
                );

        when(
                classOffering.getId()
        )
                .thenReturn(
                        classOfferingId
                );


        when(
                classOfferingRepository.findByTenantIdAndId(
                        tenantId,
                        classOfferingId
                )
        )
                .thenReturn(
                        Optional.of(
                                classOffering
                        )
                );


        when(
                repository.save(
                        any(
                                MarkSheet.class
                        )
                )
        )
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(
                                        0
                                )
                );
    }


    @Test
    void createBuildsTenantScopedCoherentMarkSheet() {

        arrangeValidParents();


        MarkSheet result =
                service.create(
                        tenantId,
                        command()
                );


        assertNotNull(
                result
        );

        assertEquals(
                "MS-2026-001",
                result.getMarkSheetReference()
        );

        assertEquals(
                componentId,
                result.getAssessmentComponentId()
        );

        assertEquals(
                subjectOfferingId,
                result.getSubjectOfferingId()
        );

        assertEquals(
                classOfferingId,
                result.getClassOfferingId()
        );

        assertEquals(
                "DRAFT",
                result.getMarkSheetStatus()
        );


        verify(
                repository
        )
                .save(
                        result
                );
    }


    @Test
    void duplicateReferenceIsRejectedBeforeParentResolution() {

        when(
                repository
                        .existsByTenantIdAndMarkSheetReference(
                                tenantId,
                                "MS-2026-001"
                        )
        )
                .thenReturn(
                        true
                );


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.create(
                                tenantId,
                                command()
                        )
        );


        verifyNoInteractions(
                assessmentComponentGateway
        );

        verify(
                repository,
                never()
        )
                .save(
                        any()
                );
    }


    @Test
    void crossTenantOrMissingAssessmentComponentIsRejected() {

        when(
                repository
                        .existsByTenantIdAndMarkSheetReference(
                                tenantId,
                                "MS-2026-001"
                        )
        )
                .thenReturn(
                        false
                );


        when(
                assessmentComponentGateway.find(
                        tenantId,
                        componentId
                )
        )
                .thenReturn(
                        Optional.empty()
                );


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.create(
                                tenantId,
                                command()
                        )
        );


        verify(
                repository,
                never()
        )
                .save(
                        any()
                );
    }


    @Test
    void subjectOfferingAndClassOfferingMustBeCoherent() {

        when(
                repository
                        .existsByTenantIdAndMarkSheetReference(
                                tenantId,
                                "MS-2026-001"
                        )
        )
                .thenReturn(
                        false
                );


        when(
                assessmentComponentGateway.find(
                        tenantId,
                        componentId
                )
        )
                .thenReturn(
                        Optional.of(
                                new AssessmentComponentReferenceGateway
                                        .AssessmentComponentReference(
                                                componentId,
                                                UUID.randomUUID(),
                                                UUID.randomUUID(),
                                                subjectOfferingId,
                                                new BigDecimal("100.00"),
                                                new BigDecimal("50.00"),
                                                "PLANNED",
                                                "ACTIVE"
                                        )
                        )
                );


        SubjectOffering subjectOffering =
                mock(
                        SubjectOffering.class
                );

        when(
                subjectOffering.getId()
        )
                .thenReturn(
                        subjectOfferingId
                );

        when(
                subjectOffering.getClassOfferingId()
        )
                .thenReturn(
                        UUID.randomUUID()
                );


        when(
                subjectOfferingRepository.findByTenantIdAndId(
                        tenantId,
                        subjectOfferingId
                )
        )
                .thenReturn(
                        Optional.of(
                                subjectOffering
                        )
                );


        ClassOffering classOffering =
                mock(
                        ClassOffering.class
                );

        when(
                classOffering.getId()
        )
                .thenReturn(
                        classOfferingId
                );


        when(
                classOfferingRepository.findByTenantIdAndId(
                        tenantId,
                        classOfferingId
                )
        )
                .thenReturn(
                        Optional.of(
                                classOffering
                        )
                );


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.create(
                                tenantId,
                                command()
                        )
        );


        verify(
                repository,
                never()
        )
                .save(
                        any()
                );
    }


    @Test
    void getUsesTenantScopedRepositoryLookup() {

        UUID markSheetId =
                UUID.randomUUID();


        MarkSheet sheet =
                new MarkSheet(
                        "MS-GET-001",
                        componentId,
                        subjectOfferingId,
                        classOfferingId,
                        new BigDecimal("100.00")
                );


        when(
                repository.findByTenantIdAndId(
                        tenantId,
                        markSheetId
                )
        )
                .thenReturn(
                        Optional.of(
                                sheet
                        )
                );


        assertSame(
                sheet,
                service.get(
                        tenantId,
                        markSheetId
                )
        );


        verify(
                repository
        )
                .findByTenantIdAndId(
                        tenantId,
                        markSheetId
                );

        verify(
                repository,
                never()
        )
                .findById(
                        any()
                );
    }


    @Test
    void openAndSubmitRecordActorThroughTenantScopedService() {

        UUID markSheetId =
                UUID.randomUUID();

        UUID actor =
                UUID.randomUUID();


        MarkSheet sheet =
                new MarkSheet(
                        "MS-LIFE-001",
                        componentId,
                        subjectOfferingId,
                        classOfferingId,
                        new BigDecimal("100.00")
                );


        when(
                repository.findByTenantIdAndId(
                        tenantId,
                        markSheetId
                )
        )
                .thenReturn(
                        Optional.of(
                                sheet
                        )
                );


        service.open(
                tenantId,
                markSheetId,
                actor
        );


        assertEquals(
                actor,
                sheet.getEntryOpenedBy()
        );


        service.submit(
                tenantId,
                markSheetId,
                actor
        );


        assertEquals(
                "SUBMITTED",
                sheet.getMarkSheetStatus()
        );

        assertEquals(
                actor,
                sheet.getSubmittedBy()
        );

        assertNotNull(
                sheet.getSubmittedAt()
        );
    }
}
