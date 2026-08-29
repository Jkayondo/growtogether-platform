package africa.growtogether.platform.school.relationship;

import africa.growtogether.platform.common.persistence.EntityStatus;

import africa.growtogether.platform.school.guardian.GuardianRepository;
import africa.growtogether.platform.school.student.StudentRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentGuardianRelationshipService {

    private final StudentGuardianRelationshipRepository repository;
    private final StudentRepository students;
    private final GuardianRepository guardians;

    public StudentGuardianRelationshipService(
            StudentGuardianRelationshipRepository repository,
            StudentRepository students,
            GuardianRepository guardians
    ) {
        this.repository = repository;
        this.students = students;
        this.guardians = guardians;
    }

    @Transactional
    public StudentGuardianRelationship create(
            UUID tenantId,
            CreateStudentGuardianRelationshipCommand command
    ) {

        students
                .findByTenantIdAndId(
                        tenantId,
                        command.studentId()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Student not found for tenant"
                        )
                );

        guardians
                .findByTenantIdAndId(
                        tenantId,
                        command.guardianId()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Guardian not found for tenant"
                        )
                );

        if (
                repository.existsByTenantIdAndStudentIdAndGuardianId(
                        tenantId,
                        command.studentId(),
                        command.guardianId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Student guardian relationship already exists"
            );
        }

        if (
                command.primaryGuardian()
                && repository
                        .findFirstByTenantIdAndStudentIdAndPrimaryGuardianTrueAndRelationshipStatusAndStatus(
                                tenantId,
                                command.studentId(),
                                "ACTIVE",
                                EntityStatus.ACTIVE
                        )
                        .isPresent()
        ) {
            throw new IllegalArgumentException(
                    "Student already has an active primary guardian"
            );
        }

        StudentGuardianRelationship relationship =
                new StudentGuardianRelationship(
                        command.studentId(),
                        command.guardianId(),
                        command.relationshipType(),
                        command.relationshipDescription(),
                        command.legalGuardian(),
                        command.primaryGuardian(),
                        command.emergencyContact(),
                        command.hasCustody(),
                        command.custodyType(),
                        command.custodyNotes(),
                        command.livesWithStudent(),
                        command.authorizedToCollect(),
                        command.receivesCommunications(),
                        command.receivesAcademicInformation(),
                        command.receivesDisciplineInformation(),
                        command.receivesMedicalInformation(),
                        command.mayApproveSchoolActivities()
                );

        relationship.setTenantId(
                tenantId
        );

        return repository.save(
                relationship
        );
    }

    @Transactional(readOnly = true)
    public StudentGuardianRelationship get(
            UUID tenantId,
            UUID relationshipId
    ) {

        return repository
                .findByTenantIdAndId(
                        tenantId,
                        relationshipId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Student guardian relationship not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<StudentGuardianRelationship> findByStudent(
            UUID tenantId,
            UUID studentId
    ) {

        return repository
                .findByTenantIdAndStudentId(
                        tenantId,
                        studentId
                );
    }

    @Transactional(readOnly = true)
    public List<StudentGuardianRelationship> findByGuardian(
            UUID tenantId,
            UUID guardianId
    ) {

        return repository
                .findByTenantIdAndGuardianId(
                        tenantId,
                        guardianId
                );
    }

    @Transactional
    public StudentGuardianRelationship suspend(
            UUID tenantId,
            UUID relationshipId
    ) {

        StudentGuardianRelationship relationship =
                get(
                        tenantId,
                        relationshipId
                );

        relationship.suspend();

        return repository.save(
                relationship
        );
    }

    @Transactional
    public StudentGuardianRelationship restrict(
            UUID tenantId,
            UUID relationshipId
    ) {

        StudentGuardianRelationship relationship =
                get(
                        tenantId,
                        relationshipId
                );

        relationship.restrict();

        return repository.save(
                relationship
        );
    }

    @Transactional
    public StudentGuardianRelationship end(
            UUID tenantId,
            UUID relationshipId,
            LocalDate effectiveTo
    ) {

        StudentGuardianRelationship relationship =
                get(
                        tenantId,
                        relationshipId
                );

        relationship.end(
                effectiveTo
        );

        return repository.save(
                relationship
        );
    }

    @Transactional
    public StudentGuardianRelationship reactivate(
            UUID tenantId,
            UUID relationshipId
    ) {

        StudentGuardianRelationship relationship =
                get(
                        tenantId,
                        relationshipId
                );

        if (
                relationship.isPrimaryGuardian()
                && repository
                        .findFirstByTenantIdAndStudentIdAndPrimaryGuardianTrueAndRelationshipStatusAndStatus(
                                tenantId,
                                relationship.getStudentId(),
                                "ACTIVE",
                                EntityStatus.ACTIVE
                        )
                        .filter(
                                active ->
                                        !active.getId().equals(
                                                relationship.getId()
                                        )
                        )
                        .isPresent()
        ) {
            throw new IllegalArgumentException(
                    "Student already has another active primary guardian"
            );
        }

        relationship.reactivate();

        return repository.save(
                relationship
        );
    }
}
