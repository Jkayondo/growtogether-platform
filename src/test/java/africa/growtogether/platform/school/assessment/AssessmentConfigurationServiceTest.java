package africa.growtogether.platform.school.assessment;

import africa.growtogether.platform.school.subject.SubjectConfiguration;
import africa.growtogether.platform.school.subject.SubjectConfigurationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AssessmentConfigurationServiceTest {

    @Mock
    private AssessmentConfigurationRepository repository;

    @Mock
    private SubjectConfigurationRepository subjectConfigurations;

    private AssessmentConfigurationService service;


    @BeforeEach
    void setUp() {
        service = new AssessmentConfigurationService(
                repository,
                subjectConfigurations
        );
    }


    @Test
    void createRequiresTenantOwnedSubjectConfiguration() {
        UUID tenantId = UUID.randomUUID();
        UUID subjectConfigurationId = UUID.randomUUID();

        when(subjectConfigurations.findByIdAndTenantId(
                subjectConfigurationId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(
                        tenantId,
                        subjectConfigurationId,
                        AssessmentType.EXAMINATION,
                        "End of Term",
                        60
                )
        );

        verify(subjectConfigurations)
                .findByIdAndTenantId(
                        subjectConfigurationId,
                        tenantId
                );

        verifyNoInteractions(repository);
    }


    @Test
    void createUsesTenantScopedDuplicateCheckAndSaves() {
        UUID tenantId = UUID.randomUUID();
        UUID subjectConfigurationId = UUID.randomUUID();

        SubjectConfiguration subjectConfiguration =
                mock(SubjectConfiguration.class);

        when(subjectConfigurations.findByIdAndTenantId(
                subjectConfigurationId,
                tenantId
        )).thenReturn(Optional.of(subjectConfiguration));

        when(repository
                .existsByTenantIdAndSubjectConfigurationIdAndAssessmentName(
                        tenantId,
                        subjectConfigurationId,
                        "End of Term"
                ))
                .thenReturn(false);

        when(repository.save(any(AssessmentConfiguration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AssessmentConfiguration result = service.create(
                tenantId,
                subjectConfigurationId,
                AssessmentType.EXAMINATION,
                "End of Term",
                60
        );

        assertNotNull(result);

        verify(repository)
                .existsByTenantIdAndSubjectConfigurationIdAndAssessmentName(
                        tenantId,
                        subjectConfigurationId,
                        "End of Term"
                );

        verify(repository).save(any(AssessmentConfiguration.class));
    }


    @Test
    void createRejectsTenantScopedDuplicate() {
        UUID tenantId = UUID.randomUUID();
        UUID subjectConfigurationId = UUID.randomUUID();

        when(subjectConfigurations.findByIdAndTenantId(
                subjectConfigurationId,
                tenantId
        )).thenReturn(Optional.of(mock(SubjectConfiguration.class)));

        when(repository
                .existsByTenantIdAndSubjectConfigurationIdAndAssessmentName(
                        tenantId,
                        subjectConfigurationId,
                        "Coursework"
                ))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> service.create(
                        tenantId,
                        subjectConfigurationId,
                        AssessmentType.COURSEWORK,
                        "Coursework",
                        40
                )
        );

        verify(repository, never())
                .save(any(AssessmentConfiguration.class));
    }


    @Test
    void getBySubjectIsTenantScoped() {
        UUID tenantId = UUID.randomUUID();
        UUID subjectConfigurationId = UUID.randomUUID();

        List<AssessmentConfiguration> expected = List.of();

        when(repository
                .findByTenantIdAndSubjectConfigurationIdOrderByAssessmentNameAsc(
                        tenantId,
                        subjectConfigurationId
                ))
                .thenReturn(expected);

        List<AssessmentConfiguration> result =
                service.getBySubject(
                        tenantId,
                        subjectConfigurationId
                );

        assertSame(expected, result);

        verify(repository)
                .findByTenantIdAndSubjectConfigurationIdOrderByAssessmentNameAsc(
                        tenantId,
                        subjectConfigurationId
                );
    }
}
