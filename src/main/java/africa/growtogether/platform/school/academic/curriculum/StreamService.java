package africa.growtogether.platform.school.academic.curriculum;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class StreamService {

    private final StreamRepository repository;
    private final CampusRepository campuses;
    private final ClassGradeRepository classGrades;

    public StreamService(
            StreamRepository repository,
            CampusRepository campuses,
            ClassGradeRepository classGrades
    ) {
        this.repository = repository;
        this.campuses = campuses;
        this.classGrades = classGrades;
    }

    @Transactional
    public Stream create(
            UUID tenantId,
            UUID campusId,
            UUID classGradeId,
            String streamCode,
            String streamName,
            Integer capacity
    ) {

        campuses
                .findByTenantIdAndId(
                        tenantId,
                        campusId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Campus not found for tenant"
                        )
                );

        classGrades
                .findByTenantIdAndId(
                        tenantId,
                        classGradeId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Class grade not found for tenant"
                        )
                );

        repository
                .findByTenantIdAndCampusIdAndClassGradeIdAndStreamCode(
                        tenantId,
                        campusId,
                        classGradeId,
                        streamCode
                )
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Stream code already exists for this campus and class grade"
                    );
                });

        if (capacity != null && capacity <= 0) {
            throw new IllegalArgumentException(
                    "Stream capacity must be greater than zero"
            );
        }

        Stream stream =
                new Stream(
                        campusId,
                        classGradeId,
                        streamCode,
                        streamName,
                        capacity
                );

        stream.setTenantId(
                tenantId
        );

        return repository.save(
                stream
        );
    }

    @Transactional(readOnly = true)
    public Stream findById(
            UUID tenantId,
            UUID streamId
    ) {

        return repository
                .findByTenantIdAndId(
                        tenantId,
                        streamId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Stream not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Stream findByCode(
            UUID tenantId,
            UUID campusId,
            UUID classGradeId,
            String streamCode
    ) {

        return repository
                .findByTenantIdAndCampusIdAndClassGradeIdAndStreamCode(
                        tenantId,
                        campusId,
                        classGradeId,
                        streamCode
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Stream not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Stream> findByCampus(
            UUID tenantId,
            UUID campusId
    ) {

        return repository
                .findByTenantIdAndCampusId(
                        tenantId,
                        campusId
                );
    }

    @Transactional(readOnly = true)
    public List<Stream> findByClassGrade(
            UUID tenantId,
            UUID classGradeId
    ) {

        return repository
                .findByTenantIdAndClassGradeId(
                        tenantId,
                        classGradeId
                );
    }

    @Transactional(readOnly = true)
    public List<Stream> findByCampusAndClassGrade(
            UUID tenantId,
            UUID campusId,
            UUID classGradeId
    ) {

        return repository
                .findByTenantIdAndCampusIdAndClassGradeId(
                        tenantId,
                        campusId,
                        classGradeId
                );
    }

    @Transactional
    public Stream activate(
            UUID tenantId,
            UUID streamId
    ) {

        Stream stream =
                findById(
                        tenantId,
                        streamId
                );

        stream.activate();

        return repository.save(
                stream
        );
    }

    @Transactional
    public Stream deactivate(
            UUID tenantId,
            UUID streamId
    ) {

        Stream stream =
                findById(
                        tenantId,
                        streamId
                );

        stream.deactivate();

        return repository.save(
                stream
        );
    }
}
