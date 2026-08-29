package africa.growtogether.platform.school.academic.teaching;

import africa.growtogether.platform.ewf.WorkforceMember;
import africa.growtogether.platform.ewf.WorkforceMemberRepository;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TeacherProfileServiceTest {

    @Test
    void createsTeacherProfileForWorkforceMemberInSameTenant() {

        TeacherProfileRepository repository =
                mock(TeacherProfileRepository.class);

        WorkforceMemberRepository workforceMembers =
                mock(WorkforceMemberRepository.class);

        TeacherProfileService service =
                new TeacherProfileService(
                        repository,
                        workforceMembers
                );

        UUID tenantId = UUID.randomUUID();
        UUID workforceMemberId = UUID.randomUUID();

        WorkforceMember workforceMember =
                mock(WorkforceMember.class);

        when(
                workforceMembers.findByTenantIdAndId(
                        tenantId,
                        workforceMemberId
                )
        ).thenReturn(
                Optional.of(workforceMember)
        );

        when(
                repository.findByTenantIdAndWorkforceMemberId(
                        tenantId,
                        workforceMemberId
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                repository.findByTenantIdAndTeacherNumber(
                        tenantId,
                        "TCH-001"
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                repository.findByTenantIdAndTeacherRegistrationNumber(
                        tenantId,
                        "REG-001"
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                repository.save(
                        any(TeacherProfile.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        TeacherProfile result =
                service.create(
                        tenantId,
                        workforceMemberId,
                        "TCH-001",
                        "REG-001",
                        "LIC-001",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2028, 12, 31),
                        "PRIMARY",
                        "ENGLISH",
                        null,
                        "CLASSROOM_TEACHER",
                        false,
                        false,
                        false,
                        30,
                        "Release 1 teacher"
                );

        assertNotNull(result);
        assertEquals(
                workforceMemberId,
                result.getWorkforceMemberId()
        );
        assertEquals(
                "TCH-001",
                result.getTeacherNumber()
        );

        verify(
                workforceMembers
        ).findByTenantIdAndId(
                tenantId,
                workforceMemberId
        );

        verify(
                repository
        ).save(
                any(TeacherProfile.class)
        );
    }

    @Test
    void rejectsWorkforceMemberNotFoundForTenant() {

        TeacherProfileRepository repository =
                mock(TeacherProfileRepository.class);

        WorkforceMemberRepository workforceMembers =
                mock(WorkforceMemberRepository.class);

        TeacherProfileService service =
                new TeacherProfileService(
                        repository,
                        workforceMembers
                );

        UUID tenantId = UUID.randomUUID();
        UUID workforceMemberId = UUID.randomUUID();

        when(
                workforceMembers.findByTenantIdAndId(
                        tenantId,
                        workforceMemberId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> createDefault(
                                service,
                                tenantId,
                                workforceMemberId
                        )
                );

        assertEquals(
                "Workforce member not found for tenant",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(TeacherProfile.class)
        );
    }

    @Test
    void rejectsDuplicateTeacherProfileForWorkforceMember() {

        TeacherProfileRepository repository =
                mock(TeacherProfileRepository.class);

        WorkforceMemberRepository workforceMembers =
                mock(WorkforceMemberRepository.class);

        TeacherProfileService service =
                new TeacherProfileService(
                        repository,
                        workforceMembers
                );

        UUID tenantId = UUID.randomUUID();
        UUID workforceMemberId = UUID.randomUUID();

        when(
                workforceMembers.findByTenantIdAndId(
                        tenantId,
                        workforceMemberId
                )
        ).thenReturn(
                Optional.of(
                        mock(WorkforceMember.class)
                )
        );

        when(
                repository.findByTenantIdAndWorkforceMemberId(
                        tenantId,
                        workforceMemberId
                )
        ).thenReturn(
                Optional.of(
                        mock(TeacherProfile.class)
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> createDefault(
                                service,
                                tenantId,
                                workforceMemberId
                        )
                );

        assertEquals(
                "Workforce member already has a teacher profile",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(TeacherProfile.class)
        );
    }

    @Test
    void rejectsDuplicateTeacherNumber() {

        TeacherProfileRepository repository =
                mock(TeacherProfileRepository.class);

        WorkforceMemberRepository workforceMembers =
                mock(WorkforceMemberRepository.class);

        TeacherProfileService service =
                new TeacherProfileService(
                        repository,
                        workforceMembers
                );

        UUID tenantId = UUID.randomUUID();
        UUID workforceMemberId = UUID.randomUUID();

        when(
                workforceMembers.findByTenantIdAndId(
                        tenantId,
                        workforceMemberId
                )
        ).thenReturn(
                Optional.of(
                        mock(WorkforceMember.class)
                )
        );

        when(
                repository.findByTenantIdAndWorkforceMemberId(
                        tenantId,
                        workforceMemberId
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                repository.findByTenantIdAndTeacherNumber(
                        tenantId,
                        "TCH-001"
                )
        ).thenReturn(
                Optional.of(
                        mock(TeacherProfile.class)
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> createDefault(
                                service,
                                tenantId,
                                workforceMemberId
                        )
                );

        assertEquals(
                "Teacher number already exists",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(TeacherProfile.class)
        );
    }

    @Test
    void rejectsDuplicateTeacherRegistrationNumber() {

        TeacherProfileRepository repository =
                mock(TeacherProfileRepository.class);

        WorkforceMemberRepository workforceMembers =
                mock(WorkforceMemberRepository.class);

        TeacherProfileService service =
                new TeacherProfileService(
                        repository,
                        workforceMembers
                );

        UUID tenantId = UUID.randomUUID();
        UUID workforceMemberId = UUID.randomUUID();

        when(
                workforceMembers.findByTenantIdAndId(
                        tenantId,
                        workforceMemberId
                )
        ).thenReturn(
                Optional.of(
                        mock(WorkforceMember.class)
                )
        );

        when(
                repository.findByTenantIdAndWorkforceMemberId(
                        tenantId,
                        workforceMemberId
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                repository.findByTenantIdAndTeacherNumber(
                        tenantId,
                        "TCH-001"
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                repository.findByTenantIdAndTeacherRegistrationNumber(
                        tenantId,
                        "REG-001"
                )
        ).thenReturn(
                Optional.of(
                        mock(TeacherProfile.class)
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> createDefault(
                                service,
                                tenantId,
                                workforceMemberId
                        )
                );

        assertEquals(
                "Teacher registration number already exists",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(TeacherProfile.class)
        );
    }

    private TeacherProfile createDefault(
            TeacherProfileService service,
            UUID tenantId,
            UUID workforceMemberId
    ) {

        return service.create(
                tenantId,
                workforceMemberId,
                "TCH-001",
                "REG-001",
                "LIC-001",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2028, 12, 31),
                "PRIMARY",
                "ENGLISH",
                null,
                "CLASSROOM_TEACHER",
                false,
                false,
                false,
                30,
                "Release 1 teacher"
        );
    }
}
