package africa.growtogether.platform.eaif.approval;

import africa.growtogether.platform.eaif.AiRequest;
import africa.growtogether.platform.eaif.AiRequestRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@Transactional
public class EaifApprovalService {


    private final EaifApprovalRecordRepository repository;

    private final AiRequestRepository requests;


    public EaifApprovalService(
            EaifApprovalRecordRepository repository,
            AiRequestRepository requests
    ) {
        this.repository = repository;
        this.requests = requests;
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


    public EaifApprovalRecord approveAndRelease(
            UUID tenantId,
            UUID aiRequestId,
            UUID approvedBy,
            String reason
    ) {

        EaifApprovalRecord record =
                approve(
                        tenantId,
                        aiRequestId,
                        approvedBy,
                        reason
                );


        AiRequest request =
                requests.findByIdAndTenantId(
                        aiRequestId,
                        tenantId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "AI request not found"
                        )
                );


        request.approve();


        return record;
    }


    public EaifApprovalRecord rejectAndBlock(
            UUID tenantId,
            UUID aiRequestId,
            UUID rejectedBy,
            String reason
    ) {

        EaifApprovalRecord record =
                reject(
                        tenantId,
                        aiRequestId,
                        rejectedBy,
                        reason
                );


        AiRequest request =
                requests.findByIdAndTenantId(
                        aiRequestId,
                        tenantId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "AI request not found"
                        )
                );


        request.reject(reason);


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
