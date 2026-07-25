package africa.growtogether.platform.eaif.audit.reporting;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eaif.AiEnums;
import africa.growtogether.platform.eaif.audit.ExecutionStatus;
import africa.growtogether.platform.eaif.audit.query.EaifAuditQueryService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai/audit")
public class EaifAuditReportingController {

    private final EaifAuditQueryService service;
    private final EnterpriseIdentityContext identity;

    public EaifAuditReportingController(
            EaifAuditQueryService service,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.identity = identity;
    }


    @GetMapping
    @PreAuthorize("hasAuthority('ai.audit.read')")
    public List<AuditReportDtos.AuditItem> all() {

        return service.findAll(
                identity.tenantId()
        );
    }


    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('ai.audit.read')")
    public List<AuditReportDtos.AuditItem> byStatus(
            @PathVariable ExecutionStatus status
    ) {

        return service.findByStatus(
                identity.tenantId(),
                status
        );
    }


    @GetMapping("/risk/{risk}")
    @PreAuthorize("hasAuthority('ai.audit.read')")
    public List<AuditReportDtos.AuditItem> byRisk(
            @PathVariable AiEnums.RiskLevel risk
    ) {

        return service.findByRisk(
                identity.tenantId(),
                risk
        );
    }


    @GetMapping("/model/{modelCode}")
    @PreAuthorize("hasAuthority('ai.audit.read')")
    public List<AuditReportDtos.AuditItem> byModel(
            @PathVariable String modelCode
    ) {

        return service.findByModel(
                identity.tenantId(),
                modelCode
        );
    }
}
