package africa.growtogether.platform.school.timetable.resource;

import africa.growtogether.platform.school.academic.curriculum.CampusRepository;
import africa.growtogether.platform.school.academic.subject.SubjectRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SchedulingResourceService {

    private final SchedulingResourceRepository repository;
    private final CampusRepository campuses;
    private final SubjectRepository subjects;

    public SchedulingResourceService(
            SchedulingResourceRepository repository,
            CampusRepository campuses,
            SubjectRepository subjects
    ) {
        this.repository = repository;
        this.campuses = campuses;
        this.subjects = subjects;
    }

    @Transactional
    public SchedulingResource create(
            UUID tenantId,
            CreateSchedulingResourceCommand command
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        campuses
                .findByTenantIdAndId(
                        tenantId,
                        command.campusId()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Campus not found for tenant"
                        )
                );

        if (
                command.specializedForSubjectId()
                        != null
        ) {

            subjects
                    .findByTenantIdAndId(
                            tenantId,
                            command.specializedForSubjectId()
                    )
                    .orElseThrow(
                            () -> new IllegalArgumentException(
                                    "Specialized subject not found for tenant"
                            )
                    );
        }

        if (
                repository
                        .existsByTenantIdAndCampusIdAndResourceCode(
                                tenantId,
                                command.campusId(),
                                command.resourceCode()
                        )
        ) {
            throw new IllegalArgumentException(
                    "Scheduling resource code already exists for campus"
            );
        }

        SchedulingResource resource =
                new SchedulingResource(
                        command.campusId(),
                        command.resourceCode(),
                        command.resourceName(),
                        command.resourceType(),
                        command.capacity(),
                        command.locationDescription(),
                        command.specializedForSubjectId(),
                        command.bookable(),
                        command.sharedResource()
                );

        resource.setTenantId(
                tenantId
        );

        return repository.save(
                resource
        );
    }

    @Transactional(readOnly = true)
    public SchedulingResource get(
            UUID tenantId,
            UUID resourceId
    ) {

        return repository
                .findByTenantIdAndId(
                        tenantId,
                        resourceId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Scheduling resource not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<SchedulingResource> findByCampus(
            UUID tenantId,
            UUID campusId
    ) {

        return repository
                .findByTenantIdAndCampusId(
                        tenantId,
                        campusId
                );
    }

    @Transactional
    public SchedulingResource markMaintenance(
            UUID tenantId,
            UUID resourceId
    ) {

        SchedulingResource resource =
                get(
                        tenantId,
                        resourceId
                );

        resource.markMaintenance();

        return repository.save(
                resource
        );
    }

    @Transactional
    public SchedulingResource markUnavailable(
            UUID tenantId,
            UUID resourceId
    ) {

        SchedulingResource resource =
                get(
                        tenantId,
                        resourceId
                );

        resource.markUnavailable();

        return repository.save(
                resource
        );
    }

    @Transactional
    public SchedulingResource activate(
            UUID tenantId,
            UUID resourceId
    ) {

        SchedulingResource resource =
                get(
                        tenantId,
                        resourceId
                );

        resource.activate();

        return repository.save(
                resource
        );
    }

    @Transactional
    public SchedulingResource retire(
            UUID tenantId,
            UUID resourceId
    ) {

        SchedulingResource resource =
                get(
                        tenantId,
                        resourceId
                );

        resource.retire();

        return repository.save(
                resource
        );
    }
}
