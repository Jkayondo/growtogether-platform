package africa.growtogether.platform.school.assessment.examination.schedule;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;


@Service
@Transactional
public class ExaminationScheduleService {


    private final ExaminationScheduleRepository repository;


    public ExaminationScheduleService(
            ExaminationScheduleRepository repository
    ) {

        this.repository = repository;

    }


    public ExaminationSchedule create(
            UUID tenantId,
            String reference,
            UUID sessionId,
            UUID paperId,
            UUID classOfferingId,
            LocalDate date,
            LocalTime start,
            LocalTime end
    ) {

        if (repository.existsByTenantIdAndScheduleReference(
                tenantId,
                reference
        )) {

            throw new IllegalArgumentException(
                    "Schedule reference already exists"
            );

        }


        ExaminationSchedule schedule =
                new ExaminationSchedule(
                        reference,
                        sessionId,
                        paperId,
                        classOfferingId,
                        date,
                        start,
                        end
                );


        return repository.save(schedule);

    }


    @Transactional(readOnly = true)
    public ExaminationSchedule get(
            UUID tenantId,
            String reference
    ) {

        return repository
                .findByTenantIdAndScheduleReference(
                        tenantId,
                        reference
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Examination schedule not found"
                        )
                );

    }

}
