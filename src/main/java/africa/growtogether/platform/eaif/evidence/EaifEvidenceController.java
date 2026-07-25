package africa.growtogether.platform.eaif.evidence;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai/evidence")
public class EaifEvidenceController {

    private final EaifEvidenceGenerationService service;
    private final EnterpriseIdentityContext identity;


    public EaifEvidenceController(
            EaifEvidenceGenerationService service,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.identity = identity;
    }


    @GetMapping("/package")
    @PreAuthorize("hasAuthority('ai.evidence.read')")
    public EaifEvidenceDtos.EvidencePackage packageEvidence() {

        return service.generate(
                identity.tenantId()
        );
    }
}
