package africa.growtogether.platform.eaif.approval;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@Transactional
public class EaifApprovalService {


    private final EaifApprovalRecordRepository repository;


    public EaifApprovalService(
            EaifApprovalRecordRepository repository
    ) {
        this.repository = repository;
    }


    public EaifApprovalRecord requestApproval(
            UUID tenantId,
            UUID aiRequestId
    ) {

        EaifApprovalRecord record =
                new EaifApprovalRecord(
                        tenantId,
                        aiRequestId
                );


        return repository.save(record);
    }


    public EaifApprovalRecord approve(
            UUID tenantId,
            UUID aiRequestId,
            UUID approvedBy,
            String reason
    ) {

        EaifApprovalRecord record =
                get(
                        tenantId,
                        aiRequestId
                );


        record.approve(
                approvedBy,
                reason
        );


        return record;
    }


    public EaifApprovalRecord reject(
            UUID tenantId,
            UUID aiRequestId,
            UUID rejectedBy,
            String reason
    ) {

        EaifApprovalRecord record =
                get(
                        tenantId,
                        aiRequestId
                );


        record.reject(
                rejectedBy,
                reason
        );


        return record;
    }


    @Transactional(readOnly = true)
    public EaifApprovalRecord get(
            UUID tenantId,
            UUID aiRequestId
    ) {

        return repository
                .findByTenantIdAndAiRequestId(
                        tenantId,
                        aiRequestId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "AI approval record not found"
                        )
                );
    }
}
