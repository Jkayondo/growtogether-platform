package africa.growtogether.platform.ewf;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WorkforceMemberServiceTest {

    @Test
    void createsWorkforceMemberForTenant() {

        WorkforceMemberRepository repository =
                mock(WorkforceMemberRepository.class);

        WorkforceMemberService service =
                new WorkforceMemberService(repository);

        UUID tenantId = UUID.randomUUID();

        when(
                repository.save(
                        any(WorkforceMember.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        WorkforceMember result =
                service.create(
                        tenantId,
                        "WF-001",
                        "EMP-001",
                        "John",
                        "K",
                        "Kayondo",
                        "John",
                        LocalDate.of(1980, 1, 1),
                        "MALE",
                        "UG",
                        "NIN-001",
                        null,
                        "+256700000001",
                        null,
                        "john@example.com",
                        "Kampala",
                        null,
                        null,
                        null,
                        "EMPLOYEE"
                );

        assertNotNull(result);

        verify(
                repository
        ).save(
                any(WorkforceMember.class)
        );
    }

    @Test
    void findsWorkforceMemberByTenantAndWorkforceNumber() {

        WorkforceMemberRepository repository =
                mock(WorkforceMemberRepository.class);

        WorkforceMemberService service =
                new WorkforceMemberService(repository);

        UUID tenantId = UUID.randomUUID();

        WorkforceMember member =
                mock(WorkforceMember.class);

        when(
                repository.findByTenantIdAndWorkforceNumber(
                        tenantId,
                        "WF-001"
                )
        ).thenReturn(
                Optional.of(member)
        );

        WorkforceMember result =
                service.findByWorkforceNumber(
                        tenantId,
                        "WF-001"
                );

        assertSame(
                member,
                result
        );

        verify(
                repository
        ).findByTenantIdAndWorkforceNumber(
                tenantId,
                "WF-001"
        );
    }

    @Test
    void rejectsMissingWorkforceNumberForTenant() {

        WorkforceMemberRepository repository =
                mock(WorkforceMemberRepository.class);

        WorkforceMemberService service =
                new WorkforceMemberService(repository);

        UUID tenantId = UUID.randomUUID();

        when(
                repository.findByTenantIdAndWorkforceNumber(
                        tenantId,
                        "WF-MISSING"
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.findByWorkforceNumber(
                                tenantId,
                                "WF-MISSING"
                        )
                );

        assertEquals(
                "Workforce member not found",
                error.getMessage()
        );
    }

    @Test
    void findsWorkforceMemberByTenantAndEmployeeNumber() {

        WorkforceMemberRepository repository =
                mock(WorkforceMemberRepository.class);

        WorkforceMemberService service =
                new WorkforceMemberService(repository);

        UUID tenantId = UUID.randomUUID();

        WorkforceMember member =
                mock(WorkforceMember.class);

        when(
                repository.findByTenantIdAndEmployeeNumber(
                        tenantId,
                        "EMP-001"
                )
        ).thenReturn(
                Optional.of(member)
        );

        WorkforceMember result =
                service.findByEmployeeNumber(
                        tenantId,
                        "EMP-001"
                );

        assertSame(
                member,
                result
        );

        verify(
                repository
        ).findByTenantIdAndEmployeeNumber(
                tenantId,
                "EMP-001"
        );
    }

    @Test
    void rejectsMissingEmployeeNumberForTenant() {

        WorkforceMemberRepository repository =
                mock(WorkforceMemberRepository.class);

        WorkforceMemberService service =
                new WorkforceMemberService(repository);

        UUID tenantId = UUID.randomUUID();

        when(
                repository.findByTenantIdAndEmployeeNumber(
                        tenantId,
                        "EMP-MISSING"
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.findByEmployeeNumber(
                                tenantId,
                                "EMP-MISSING"
                        )
                );

        assertEquals(
                "Workforce member not found",
                error.getMessage()
        );
    }

    @Test
    void findsWorkforceMembersByTenantAndEmail() {

        WorkforceMemberRepository repository =
                mock(WorkforceMemberRepository.class);

        WorkforceMemberService service =
                new WorkforceMemberService(repository);

        UUID tenantId = UUID.randomUUID();

        WorkforceMember first =
                mock(WorkforceMember.class);

        WorkforceMember second =
                mock(WorkforceMember.class);

        List<WorkforceMember> members =
                List.of(
                        first,
                        second
                );

        when(
                repository.findByTenantIdAndEmail(
                        tenantId,
                        "teacher@example.com"
                )
        ).thenReturn(
                members
        );

        List<WorkforceMember> result =
                service.findByEmail(
                        tenantId,
                        "teacher@example.com"
                );

        assertEquals(
                2,
                result.size()
        );

        assertSame(
                members,
                result
        );

        verify(
                repository
        ).findByTenantIdAndEmail(
                tenantId,
                "teacher@example.com"
        );
    }
}
