package africa.growtogether.platform.school.finance.payment;

import static africa.growtogether.platform.school.finance.payment.FinanceStudentPaymentDtos.CreateRequest;
import static africa.growtogether.platform.school.finance.payment.FinanceStudentPaymentDtos.PaymentResponse;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FinanceStudentPaymentService {

    private static final Set<String> ALLOWED_PAYMENT_METHODS =
            Set.of("BANK_DEPOSIT", "BANK_TRANSFER", "CARD", "CASH", "CHEQUE", "CREDIT_BALANCE", "DIRECT_DEBIT", "MOBILE_MONEY", "OTHER", "SALARY_DEDUCTION", "SCHOLARSHIP", "SPONSOR");

    private static final String INITIAL_STATUS =
            null;

    private final FinanceStudentPaymentJdbcRepository repository;

    public FinanceStudentPaymentService(
            FinanceStudentPaymentJdbcRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    public PaymentResponse create(
            UUID tenantId,
            CreateRequest request,
            String actor
    ) {
        requireTenant(tenantId);

        if (actor == null || actor.isBlank()) {
            throw badRequest(
                    "actor must not be blank"
            );
        }

        String normalizedActor =
                actor.trim();

        if (request == null) {
            throw badRequest(
                    "Payment request is required"
            );
        }

        if (request.studentFinancialAccountId() == null) {
            throw badRequest(
                    "studentFinancialAccountId is required"
            );
        }

        if (request.amount() == null
                || request.amount().signum() <= 0) {
            throw badRequest(
                    "Payment amount must be greater than zero"
            );
        }

                if (request.paymentMethod() == null
            || request.paymentMethod().isBlank()) {
        throw badRequest(
                "Payment method is required"
        );
    }
    if (request.paymentMethod() != null
            && !request.paymentMethod().isBlank()
            && !ALLOWED_PAYMENT_METHODS.contains(
                    request.paymentMethod()
            )) {
        throw badRequest(
                "Unsupported payment method"
        );
    }

                if (request.paymentReference() == null
            || request.paymentReference().isBlank()) {
        throw badRequest(
                "Payment reference is required"
        );
    }

                if (request.paidAt() == null) {
        throw badRequest(
                "Payment time is required"
        );
    }

        var account = repository
                .findFinancialAccountContext(
                        tenantId,
                        request.studentFinancialAccountId()
                )
                .orElseThrow(() -> notFound(
                        "Financial account not found"
                ));

        if (account.currencyCode() == null
                || account.currencyCode().isBlank()) {
            throw badRequest(
                    "Financial account currency is unavailable"
            );
        }

        try {
            return repository.insert(
                    tenantId,
                    account,
                    request,
                    INITIAL_STATUS,
                    normalizedActor
            );
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Student payment conflicts with existing finance data",
                    ex
            );
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponse get(
            UUID tenantId,
            UUID paymentId
    ) {
        requireTenant(tenantId);

        if (paymentId == null) {
            throw badRequest(
                    "paymentId is required"
            );
        }

        return repository
                .findById(
                        tenantId,
                        paymentId
                )
                .orElseThrow(() -> notFound(
                        "Student payment not found"
                ));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listByFinancialAccount(
            UUID tenantId,
            UUID studentFinancialAccountId
    ) {
        requireTenant(tenantId);

        if (studentFinancialAccountId == null) {
            throw badRequest(
                    "studentFinancialAccountId is required"
            );
        }

        repository.findFinancialAccountContext(
                tenantId,
                studentFinancialAccountId
        ).orElseThrow(() -> notFound(
                "Financial account not found"
        ));

        return repository.findByFinancialAccount(
                tenantId,
                studentFinancialAccountId
        );
    }

    private static void requireTenant(
            UUID tenantId
    ) {
        if (tenantId == null) {
            throw badRequest(
                    "X-Tenant-ID is required"
            );
        }
    }

    private static ResponseStatusException badRequest(
            String message
    ) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    private static ResponseStatusException notFound(
            String message
    ) {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                message
        );
    }
}
