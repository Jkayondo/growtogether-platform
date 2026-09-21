package africa.growtogether.platform.school.finance.statement;

import static africa.growtogether.platform.school.finance.statement.FinanceStudentAccountStatementDtos.*;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/school/finance/students/{studentId}"
)
public class FinanceStudentAccountStatementController {

    private final FinanceStudentAccountStatementService service;

    public FinanceStudentAccountStatementController(
            FinanceStudentAccountStatementService service
    ) {
        this.service = service;
    }

    @GetMapping("/account-summary")
    @PreAuthorize("hasAuthority('school.finance.read')")
    public AccountSummary accountSummary(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID studentId,
            @RequestParam String currencyCode,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate asOfDate
    ) {
        return service.accountSummary(
                tenantId,
                studentId,
                currencyCode,
                asOfDate
        );
    }

    @GetMapping("/account-statement")
    @PreAuthorize("hasAuthority('school.finance.read')")
    public AccountStatement accountStatement(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID studentId,
            @RequestParam String currencyCode,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate asOfDate
    ) {
        return service.accountStatement(
                tenantId,
                studentId,
                currencyCode,
                fromDate,
                toDate,
                asOfDate
        );
    }

    @GetMapping("/arrears")
    @PreAuthorize("hasAuthority('school.finance.read')")
    public ArrearsView arrears(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID studentId,
            @RequestParam String currencyCode,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate asOfDate
    ) {
        return service.arrears(
                tenantId,
                studentId,
                currencyCode,
                asOfDate
        );
    }
}
