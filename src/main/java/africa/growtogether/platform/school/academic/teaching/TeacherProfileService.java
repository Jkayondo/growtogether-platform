package africa.growtogether.platform.school.academic.teaching;

import africa.growtogether.platform.ewf.WorkforceMemberRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TeacherProfileService {

    private final TeacherProfileRepository repository;
    private final WorkforceMemberRepository workforceMembers;

    public TeacherProfileService(
            TeacherProfileRepository repository,
            WorkforceMemberRepository workforceMembers
    ) {
        this.repository = repository;
        this.workforceMembers = workforceMembers;
    }

    @Transactional
    public TeacherProfile create(
            UUID tenantId,
            UUID workforceMemberId,
            String teacherNumber,
            String teacherRegistrationNumber,
            String teachingLicenceNumber,
            LocalDate teachingLicenceIssuedAt,
            LocalDate teachingLicenceExpiresAt,
            String highestTeachingLevel,
            String primarySpecialization,
            String secondarySpecialization,
            String teacherCategory,
            boolean qualifiedForBoardingDuty,
            boolean qualifiedForSpecialNeeds,
            boolean qualifiedForCounselling,
            Integer maximumWeeklyPeriods,
            String notes
    ) {

        workforceMembers
                .findByTenantIdAndId(
                        tenantId,
                        workforceMemberId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Workforce member not found for tenant"
                        )
                );

        repository
                .findByTenantIdAndWorkforceMemberId(
                        tenantId,
                        workforceMemberId
                )
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Workforce member already has a teacher profile"
                    );
                });

        repository
                .findByTenantIdAndTeacherNumber(
                        tenantId,
                        teacherNumber
                )
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Teacher number already exists"
                    );
                });

        if (teacherRegistrationNumber != null
                && !teacherRegistrationNumber.isBlank()) {

            repository
                    .findByTenantIdAndTeacherRegistrationNumber(
                            tenantId,
                            teacherRegistrationNumber
                    )
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException(
                                "Teacher registration number already exists"
                        );
                    });
        }

        TeacherProfile profile =
                new TeacherProfile(
                        workforceMemberId,
                        teacherNumber,
                        teacherRegistrationNumber,
                        teachingLicenceNumber,
                        teachingLicenceIssuedAt,
                        teachingLicenceExpiresAt,
                        highestTeachingLevel,
                        primarySpecialization,
                        secondarySpecialization,
                        teacherCategory,
                        qualifiedForBoardingDuty,
                        qualifiedForSpecialNeeds,
                        qualifiedForCounselling,
                        maximumWeeklyPeriods,
                        notes
                );

        profile.setTenantId(
                tenantId
        );

        return repository.save(
                profile
        );
    }

    @Transactional(readOnly = true)
    public TeacherProfile findByTeacherNumber(
            UUID tenantId,
            String teacherNumber
    ) {

        return repository
                .findByTenantIdAndTeacherNumber(
                        tenantId,
                        teacherNumber
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Teacher profile not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public TeacherProfile findByWorkforceMember(
            UUID tenantId,
            UUID workforceMemberId
    ) {

        return repository
                .findByTenantIdAndWorkforceMemberId(
                        tenantId,
                        workforceMemberId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Teacher profile not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<TeacherProfile> findByCategory(
            UUID tenantId,
            String teacherCategory
    ) {

        return repository.findByTenantIdAndTeacherCategory(
                tenantId,
                teacherCategory
        );
    }

    @Transactional(readOnly = true)
    public List<TeacherProfile> findByTeachingStatus(
            UUID tenantId,
            String teachingStatus
    ) {

        return repository.findByTenantIdAndTeachingStatus(
                tenantId,
                teachingStatus
        );
    }

    @Transactional
    public TeacherProfile activate(
            TeacherProfile profile
    ) {

        profile.activate();

        return repository.save(
                profile
        );
    }

    @Transactional
    public TeacherProfile deactivate(
            TeacherProfile profile
    ) {

        profile.deactivate();

        return repository.save(
                profile
        );
    }
}
