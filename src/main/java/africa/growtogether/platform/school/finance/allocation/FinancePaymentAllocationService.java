
package africa.growtogether.platform.school.finance.allocation;

import static africa.growtogether.platform.school.finance.allocation.FinancePaymentAllocationDtos.AllocationResponse;
import static africa.growtogether.platform.school.finance.allocation.FinancePaymentAllocationDtos.CreateRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinancePaymentAllocationService {

    private final FinancePaymentAllocationJdbcRepository repository;

    public FinancePaymentAllocationService(
            FinancePaymentAllocationJdbcRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    public AllocationResponse create(
            UUID tenantId,
            UUID paymentId,
            CreateRequest request,
            UUID actorId
    ) {
        Objects.requireNonNull(
                tenantId,
                "tenantId is required"
        );

        Objects.requireNonNull(
                paymentId,
                "paymentId is required"
        );

        Objects.requireNonNull(
                request,
                "request is required"
        );

        Objects.requireNonNull(
                actorId,
                "actorId is required"
        );

        Objects.requireNonNull(
                request.invoiceId(),
                "invoiceId is required"
        );

        BigDecimal amount = Objects.requireNonNull(
                request.allocatedAmount(),
                "allocatedAmount is required"
        );

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "allocatedAmount must be greater than zero"
            );
        }

        return repository.create(
                tenantId,
                paymentId,
                request,
                actorId,
                actorId.toString()
        );
    }

    @Transactional(readOnly = true)
    public AllocationResponse get(
            UUID tenantId,
            UUID paymentId,
            UUID allocationId
    ) {
        Objects.requireNonNull(
                tenantId,
                "tenantId is required"
        );

        Objects.requireNonNull(
                paymentId,
                "paymentId is required"
        );

        Objects.requireNonNull(
                allocationId,
                "allocationId is required"
        );

        return repository.get(
                tenantId,
                paymentId,
                allocationId
        );
    }

    @Transactional(readOnly = true)
    public List<AllocationResponse> list(
            UUID tenantId,
            UUID paymentId
    ) {
        Objects.requireNonNull(
                tenantId,
                "tenantId is required"
        );

        Objects.requireNonNull(
                paymentId,
                "paymentId is required"
        );

        return repository.list(
                tenantId,
                paymentId
        );
    }
}
