package africa.growtogether.platform.school.academic.curriculum;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StreamServiceTest {

    @Test
    void createsStreamForCampusAndClassGradeInSameTenant() {

        StreamRepository repository =
                mock(StreamRepository.class);

        CampusRepository campuses =
                mock(CampusRepository.class);

        ClassGradeRepository classGrades =
                mock(ClassGradeRepository.class);

        StreamService service =
                new StreamService(
                        repository,
                        campuses,
                        classGrades
                );

        UUID tenantId = UUID.randomUUID();
        UUID campusId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();

        when(
                campuses.findByTenantIdAndId(
                        tenantId,
                        campusId
                )
        ).thenReturn(
                Optional.of(
                        mock(Campus.class)
                )
        );

        when(
                classGrades.findByTenantIdAndId(
                        tenantId,
                        classGradeId
                )
        ).thenReturn(
                Optional.of(
                        mock(ClassGrade.class)
                )
        );

        when(
                repository.findByTenantIdAndCampusIdAndClassGradeIdAndStreamCode(
                        tenantId,
                        campusId,
                        classGradeId,
                        "A"
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                repository.save(
                        any(Stream.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        Stream result =
                service.create(
                        tenantId,
                        campusId,
                        classGradeId,
                        "A",
                        "Stream A",
                        40
                );

        assertNotNull(result);

        assertEquals(
                campusId,
                result.getCampusId()
        );

        assertEquals(
                classGradeId,
                result.getClassGradeId()
        );

        assertEquals(
                "A",
                result.getStreamCode()
        );

        assertEquals(
                "Stream A",
                result.getStreamName()
        );

        assertEquals(
                40,
                result.getCapacity()
        );

        verify(
                repository
        ).save(
                any(Stream.class)
        );
    }

    @Test
    void rejectsCampusNotFoundForTenant() {

        StreamRepository repository =
                mock(StreamRepository.class);

        CampusRepository campuses =
                mock(CampusRepository.class);

        ClassGradeRepository classGrades =
                mock(ClassGradeRepository.class);

        StreamService service =
                new StreamService(
                        repository,
                        campuses,
                        classGrades
                );

        UUID tenantId = UUID.randomUUID();
        UUID campusId = UUID.randomUUID();

        when(
                campuses.findByTenantIdAndId(
                        tenantId,
                        campusId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                campusId,
                                UUID.randomUUID(),
                                "A",
                                "Stream A",
                                40
                        )
                );

        assertEquals(
                "Campus not found for tenant",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(Stream.class)
        );
    }

    @Test
    void rejectsClassGradeNotFoundForTenant() {

        StreamRepository repository =
                mock(StreamRepository.class);

        CampusRepository campuses =
                mock(CampusRepository.class);

        ClassGradeRepository classGrades =
                mock(ClassGradeRepository.class);

        StreamService service =
                new StreamService(
                        repository,
                        campuses,
                        classGrades
                );

        UUID tenantId = UUID.randomUUID();
        UUID campusId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();

        when(
                campuses.findByTenantIdAndId(
                        tenantId,
                        campusId
                )
        ).thenReturn(
                Optional.of(
                        mock(Campus.class)
                )
        );

        when(
                classGrades.findByTenantIdAndId(
                        tenantId,
                        classGradeId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                campusId,
                                classGradeId,
                                "A",
                                "Stream A",
                                40
                        )
                );

        assertEquals(
                "Class grade not found for tenant",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(Stream.class)
        );
    }

    @Test
    void rejectsDuplicateStreamCodeForCampusAndClassGrade() {

        StreamRepository repository =
                mock(StreamRepository.class);

        CampusRepository campuses =
                mock(CampusRepository.class);

        ClassGradeRepository classGrades =
                mock(ClassGradeRepository.class);

        StreamService service =
                new StreamService(
                        repository,
                        campuses,
                        classGrades
                );

        UUID tenantId = UUID.randomUUID();
        UUID campusId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();

        when(
                campuses.findByTenantIdAndId(
                        tenantId,
                        campusId
                )
        ).thenReturn(
                Optional.of(
                        mock(Campus.class)
                )
        );

        when(
                classGrades.findByTenantIdAndId(
                        tenantId,
                        classGradeId
                )
        ).thenReturn(
                Optional.of(
                        mock(ClassGrade.class)
                )
        );

        when(
                repository.findByTenantIdAndCampusIdAndClassGradeIdAndStreamCode(
                        tenantId,
                        campusId,
                        classGradeId,
                        "A"
                )
        ).thenReturn(
                Optional.of(
                        mock(Stream.class)
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                campusId,
                                classGradeId,
                                "A",
                                "Stream A",
                                40
                        )
                );

        assertEquals(
                "Stream code already exists for this campus and class grade",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(Stream.class)
        );
    }

    @Test
    void rejectsNonPositiveStreamCapacity() {

        StreamRepository repository =
                mock(StreamRepository.class);

        CampusRepository campuses =
                mock(CampusRepository.class);

        ClassGradeRepository classGrades =
                mock(ClassGradeRepository.class);

        StreamService service =
                new StreamService(
                        repository,
                        campuses,
                        classGrades
                );

        UUID tenantId = UUID.randomUUID();
        UUID campusId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();

        when(
                campuses.findByTenantIdAndId(
                        tenantId,
                        campusId
                )
        ).thenReturn(
                Optional.of(
                        mock(Campus.class)
                )
        );

        when(
                classGrades.findByTenantIdAndId(
                        tenantId,
                        classGradeId
                )
        ).thenReturn(
                Optional.of(
                        mock(ClassGrade.class)
                )
        );

        when(
                repository.findByTenantIdAndCampusIdAndClassGradeIdAndStreamCode(
                        tenantId,
                        campusId,
                        classGradeId,
                        "A"
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                campusId,
                                classGradeId,
                                "A",
                                "Stream A",
                                0
                        )
                );

        assertEquals(
                "Stream capacity must be greater than zero",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(Stream.class)
        );
    }

    @Test
    void activatesStreamUsingTenantScopedLookup() {

        StreamRepository repository =
                mock(StreamRepository.class);

        CampusRepository campuses =
                mock(CampusRepository.class);

        ClassGradeRepository classGrades =
                mock(ClassGradeRepository.class);

        StreamService service =
                new StreamService(
                        repository,
                        campuses,
                        classGrades
                );

        UUID tenantId = UUID.randomUUID();
        UUID streamId = UUID.randomUUID();

        Stream stream =
                new Stream(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "A",
                        "Stream A",
                        40
                );

        stream.setTenantId(
                tenantId
        );

        stream.deactivate();

        when(
                repository.findByTenantIdAndId(
                        tenantId,
                        streamId
                )
        ).thenReturn(
                Optional.of(stream)
        );

        when(
                repository.save(stream)
        ).thenReturn(
                stream
        );

        Stream result =
                service.activate(
                        tenantId,
                        streamId
                );

        assertSame(
                stream,
                result
        );

        assertEquals(
                africa.growtogether.platform.common.persistence.EntityStatus.ACTIVE,
                result.getStatus()
        );

        verify(
                repository
        ).findByTenantIdAndId(
                tenantId,
                streamId
        );

        verify(
                repository
        ).save(
                stream
        );
    }

    @Test
    void deactivatesStreamUsingTenantScopedLookup() {

        StreamRepository repository =
                mock(StreamRepository.class);

        CampusRepository campuses =
                mock(CampusRepository.class);

        ClassGradeRepository classGrades =
                mock(ClassGradeRepository.class);

        StreamService service =
                new StreamService(
                        repository,
                        campuses,
                        classGrades
                );

        UUID tenantId = UUID.randomUUID();
        UUID streamId = UUID.randomUUID();

        Stream stream =
                new Stream(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "A",
                        "Stream A",
                        40
                );

        stream.setTenantId(
                tenantId
        );

        when(
                repository.findByTenantIdAndId(
                        tenantId,
                        streamId
                )
        ).thenReturn(
                Optional.of(stream)
        );

        when(
                repository.save(stream)
        ).thenReturn(
                stream
        );

        Stream result =
                service.deactivate(
                        tenantId,
                        streamId
                );

        assertSame(
                stream,
                result
        );

        assertEquals(
                africa.growtogether.platform.common.persistence.EntityStatus.INACTIVE,
                result.getStatus()
        );

        verify(
                repository
        ).findByTenantIdAndId(
                tenantId,
                streamId
        );

        verify(
                repository
        ).save(
                stream
        );
    }

}
