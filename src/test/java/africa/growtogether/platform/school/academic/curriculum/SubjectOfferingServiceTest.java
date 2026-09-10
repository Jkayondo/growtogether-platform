package africa.growtogether.platform.school.academic.curriculum;

import africa.growtogether.platform.school.academic.subject.Subject;
import africa.growtogether.platform.school.academic.subject.SubjectService;
import africa.growtogether.platform.school.academic.term.AcademicTermService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class SubjectOfferingServiceTest {

    private SubjectOfferingRepository repository;
    private ClassOfferingService classOfferings;
    private SubjectService subjects;
    private AcademicTermService academicTerms;
    private StreamService streams;
    private SubjectOfferingService service;


    @BeforeEach
    void setUp() {

        repository =
                mock(SubjectOfferingRepository.class);

        classOfferings =
                mock(ClassOfferingService.class);

        subjects =
                mock(SubjectService.class);

        academicTerms =
                mock(AcademicTermService.class);

        streams =
                mock(StreamService.class);

        service =
                new SubjectOfferingService(
                        repository,
                        classOfferings,
                        subjects,
                        academicTerms,
                        streams
                );
    }


    @Test
    void validRequiredParentsAllowSave() {

        UUID tenantId =
                UUID.randomUUID();

        UUID classOfferingId =
                UUID.randomUUID();

        UUID subjectId =
                UUID.randomUUID();

        when(
                classOfferings.get(
                        tenantId,
                        classOfferingId
                )
        ).thenReturn(
                mock(ClassOffering.class)
        );

        when(
                subjects.get(
                        tenantId,
                        subjectId
                )
        ).thenReturn(
                mock(Subject.class)
        );

        when(
                repository.save(
                        any(SubjectOffering.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );


        SubjectOffering created =
                service.create(
                        tenantId,
                        "TEST-SUBJECT-OFFERING",
                        classOfferingId,
                        null,
                        null,
                        subjectId,
                        null,
                        null,
                        5,
                        null,
                        5,
                        35
                );


        assertEquals(
                tenantId,
                created.getTenantId()
        );

        assertEquals(
                classOfferingId,
                created.getClassOfferingId()
        );

        assertEquals(
                subjectId,
                created.getSubjectId()
        );

        assertEquals(
                "TEST-SUBJECT-OFFERING",
                created.getSubjectOfferingCode()
        );

        verify(
                classOfferings
        ).get(
                tenantId,
                classOfferingId
        );

        verify(
                subjects
        ).get(
                tenantId,
                subjectId
        );

        verify(
                repository
        ).save(
                any(SubjectOffering.class)
        );
    }


    @Test
    void wrongTenantClassOfferingIsRejectedBeforeSave() {

        UUID tenantId =
                UUID.randomUUID();

        UUID classOfferingId =
                UUID.randomUUID();

        UUID subjectId =
                UUID.randomUUID();

        when(
                classOfferings.get(
                        tenantId,
                        classOfferingId
                )
        ).thenThrow(
                new IllegalArgumentException(
                        "Class offering not found for tenant"
                )
        );


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.create(
                                tenantId,
                                "TEST-SUBJECT-OFFERING",
                                classOfferingId,
                                null,
                                null,
                                subjectId,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        )
        );


        verify(
                subjects,
                never()
        ).get(
                any(),
                any()
        );

        verify(
                repository,
                never()
        ).save(
                any(SubjectOffering.class)
        );
    }


    @Test
    void wrongTenantAcademicTermIsRejectedBeforeSave() {

        UUID tenantId =
                UUID.randomUUID();

        UUID classOfferingId =
                UUID.randomUUID();

        UUID subjectId =
                UUID.randomUUID();

        UUID academicTermId =
                UUID.randomUUID();

        when(
                classOfferings.get(
                        tenantId,
                        classOfferingId
                )
        ).thenReturn(
                mock(ClassOffering.class)
        );

        when(
                subjects.get(
                        tenantId,
                        subjectId
                )
        ).thenReturn(
                mock(Subject.class)
        );

        when(
                academicTerms.get(
                        tenantId,
                        academicTermId
                )
        ).thenThrow(
                new IllegalArgumentException(
                        "Academic term not found for tenant"
                )
        );


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.create(
                                tenantId,
                                "TEST-SUBJECT-OFFERING",
                                classOfferingId,
                                academicTermId,
                                null,
                                subjectId,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        )
        );


        verify(
                streams,
                never()
        ).findById(
                any(),
                any()
        );

        verify(
                repository,
                never()
        ).save(
                any(SubjectOffering.class)
        );
    }



    @Test
    void wrongTenantSubjectIsRejectedBeforeSave() {

        UUID tenantId =
                UUID.randomUUID();

        UUID classOfferingId =
                UUID.randomUUID();

        UUID subjectId =
                UUID.randomUUID();

        when(
                classOfferings.get(
                        tenantId,
                        classOfferingId
                )
        ).thenReturn(
                mock(ClassOffering.class)
        );

        when(
                subjects.get(
                        tenantId,
                        subjectId
                )
        ).thenThrow(
                new IllegalArgumentException(
                        "Subject not found for tenant"
                )
        );


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.create(
                                tenantId,
                                "TEST-SUBJECT-OFFERING",
                                classOfferingId,
                                null,
                                null,
                                subjectId,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        )
        );


        verify(
                classOfferings
        ).get(
                tenantId,
                classOfferingId
        );

        verify(
                repository,
                never()
        ).save(
                any(SubjectOffering.class)
        );
    }

    @Test
    void wrongTenantStreamIsRejectedBeforeSave() {

        UUID tenantId =
                UUID.randomUUID();

        UUID classOfferingId =
                UUID.randomUUID();

        UUID subjectId =
                UUID.randomUUID();

        UUID streamId =
                UUID.randomUUID();

        when(
                classOfferings.get(
                        tenantId,
                        classOfferingId
                )
        ).thenReturn(
                mock(ClassOffering.class)
        );

        when(
                subjects.get(
                        tenantId,
                        subjectId
                )
        ).thenReturn(
                mock(Subject.class)
        );

        when(
                streams.findById(
                        tenantId,
                        streamId
                )
        ).thenThrow(
                new IllegalArgumentException(
                        "Stream not found"
                )
        );


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.create(
                                tenantId,
                                "TEST-SUBJECT-OFFERING",
                                classOfferingId,
                                null,
                                streamId,
                                subjectId,
                                null,
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
                any(SubjectOffering.class)
        );
    }



}
