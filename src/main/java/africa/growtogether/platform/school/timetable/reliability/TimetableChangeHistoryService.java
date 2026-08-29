package africa.growtogether.platform.school.timetable.reliability;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TimetableChangeHistoryService {

    private final TimetableChangeHistoryRepository repository;

    public TimetableChangeHistoryService(
            TimetableChangeHistoryRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    public TimetableChangeHistory record(
            UUID tenantId,
            UUID timetableId,
            UUID timetableEntryId,
            String changeType,
            String changeReason,
            UUID changedBy,
            UUID workflowInstanceId,
            String correlationId,
            boolean notificationRequired,
            String createdBy
    ) {

        TimetableChangeHistory history =
                new TimetableChangeHistory(
                        tenantId,
                        timetableId,
                        timetableEntryId,
                        changeType,
                        changeReason,
                        changedBy,
                        workflowInstanceId,
                        correlationId,
                        notificationRequired,
                        createdBy
                );

        return repository.save(
                history
        );
    }

    @Transactional(readOnly = true)
    public List<TimetableChangeHistory> history(
            UUID tenantId,
            UUID timetableId
    ) {

        return repository
                .findByTenantIdAndTimetableIdOrderByEffectiveAtDesc(
                        tenantId,
                        timetableId
                );
    }
}
