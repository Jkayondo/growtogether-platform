package africa.growtogether.platform.school.timetable.resource;

import africa.growtogether.platform.school.academic.curriculum.Campus;
import africa.growtogether.platform.school.academic.curriculum.CampusRepository;
import africa.growtogether.platform.school.academic.subject.Subject;
import africa.growtogether.platform.school.academic.subject.SubjectRepository;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SchedulingResourceServiceTest {

    @Test
    void createsTenantScopedSchedulingResource() {

        Fixture f = new Fixture();

        f.stubCampus();

        when(
                f.repository
                        .existsByTenantIdAndCampusIdAndResourceCode(
                                f.tenantId,
                                f.campusId,
                                "LAB-01"
                        )
        ).thenReturn(false);

        when(
                f.repository.save(
                        any(SchedulingResource.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        SchedulingResource result =
                f.service.create(
                        f.tenantId,
                        f.command(
                                null,
                                "laboratory",
                                40
                        )
                );

        assertNotNull(result);

        assertEquals(
                f.campusId,
                result.getCampusId()
        );

        assertEquals(
                "LAB-01",
                result.getResourceCode()
        );

        assertEquals(
                "Science Laboratory",
                result.getResourceName()
        );

        /*
         * IMPROVEMENT:
         * controlled resource values are normalized.
         */
        assertEquals(
                "LABORATORY",
                result.getResourceType()
        );

        assertEquals(
                40,
                result.getCapacity()
        );

        assertTrue(
                result.isBookable()
        );

        assertEquals(
                "ACTIVE",
                result.getResourceStatus()
        );

        verify(
                f.campuses
        ).findByTenantIdAndId(
                f.tenantId,
                f.campusId
        );

        verify(
                f.repository
        ).save(
                any(SchedulingResource.class)
        );
    }

    @Test
    void rejectsCampusOutsideTenantBoundary() {

        Fixture f = new Fixture();

        when(
                f.campuses.findByTenantIdAndId(
                        f.tenantId,
                        f.campusId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        null,
                                        "CLASSROOM",
                                        30
                                )
                        )
                );

        assertEquals(
                "Campus not found for tenant",
                error.getMessage()
        );

        verifyNoInteractions(
                f.subjects
        );

        verify(
                f.repository,
                never()
        ).save(
                any(SchedulingResource.class)
        );
    }

    @Test
    void validatesSpecializedSubjectWithinSameTenant() {

        Fixture f = new Fixture();

        f.stubCampus();

        UUID subjectId =
                UUID.randomUUID();

        when(
                f.subjects.findByTenantIdAndId(
                        f.tenantId,
                        subjectId
                )
        ).thenReturn(
                Optional.of(
                        mock(Subject.class)
                )
        );

        when(
                f.repository
                        .existsByTenantIdAndCampusIdAndResourceCode(
                                f.tenantId,
                                f.campusId,
                                "LAB-01"
                        )
        ).thenReturn(false);

        when(
                f.repository.save(
                        any(SchedulingResource.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        SchedulingResource result =
                f.service.create(
                        f.tenantId,
                        f.command(
                                subjectId,
                                "LABORATORY",
                                35
                        )
                );

        assertEquals(
                subjectId,
                result.getSpecializedForSubjectId()
        );

        verify(
                f.subjects
        ).findByTenantIdAndId(
                f.tenantId,
                subjectId
        );
    }

    @Test
    void rejectsSpecializedSubjectOutsideTenantBoundary() {

        Fixture f = new Fixture();

        f.stubCampus();

        UUID subjectId =
                UUID.randomUUID();

        when(
                f.subjects.findByTenantIdAndId(
                        f.tenantId,
                        subjectId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        subjectId,
                                        "LABORATORY",
                                        35
                                )
                        )
                );

        assertEquals(
                "Specialized subject not found for tenant",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(SchedulingResource.class)
        );
    }

    @Test
    void rejectsDuplicateResourceCodeWithinCampus() {

        Fixture f = new Fixture();

        f.stubCampus();

        when(
                f.repository
                        .existsByTenantIdAndCampusIdAndResourceCode(
                                f.tenantId,
                                f.campusId,
                                "LAB-01"
                        )
        ).thenReturn(true);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        null,
                                        "LABORATORY",
                                        40
                                )
                        )
                );

        assertEquals(
                "Scheduling resource code already exists for campus",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(SchedulingResource.class)
        );
    }

    @Test
    void rejectsUnsupportedResourceType() {

        Fixture f = new Fixture();

        f.stubCampus();

        when(
                f.repository
                        .existsByTenantIdAndCampusIdAndResourceCode(
                                f.tenantId,
                                f.campusId,
                                "LAB-01"
                        )
        ).thenReturn(false);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        null,
                                        "SWIMMING_POOL",
                                        40
                                )
                        )
                );

        assertEquals(
                "Invalid resource type: SWIMMING_POOL",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(SchedulingResource.class)
        );
    }

    @Test
    void rejectsNonPositiveCapacity() {

        Fixture f = new Fixture();

        f.stubCampus();

        when(
                f.repository
                        .existsByTenantIdAndCampusIdAndResourceCode(
                                f.tenantId,
                                f.campusId,
                                "LAB-01"
                        )
        ).thenReturn(false);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        null,
                                        "LABORATORY",
                                        0
                                )
                        )
                );

        assertEquals(
                "capacity must be greater than zero",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(SchedulingResource.class)
        );
    }

    private static class Fixture {

        final SchedulingResourceRepository repository =
                mock(SchedulingResourceRepository.class);

        final CampusRepository campuses =
                mock(CampusRepository.class);

        final SubjectRepository subjects =
                mock(SubjectRepository.class);

        final SchedulingResourceService service =
                new SchedulingResourceService(
                        repository,
                        campuses,
                        subjects
                );

        final UUID tenantId =
                UUID.randomUUID();

        final UUID campusId =
                UUID.randomUUID();

        void stubCampus() {

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
        }

        CreateSchedulingResourceCommand command(
                UUID specializedSubjectId,
                String resourceType,
                Integer capacity
        ) {

            return new CreateSchedulingResourceCommand(
                    campusId,
                    "LAB-01",
                    "Science Laboratory",
                    resourceType,
                    capacity,
                    "Science Block",
                    specializedSubjectId,
                    true,
                    false
            );
        }
    }
}
