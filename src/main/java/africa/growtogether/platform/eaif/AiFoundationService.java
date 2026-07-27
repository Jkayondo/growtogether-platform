package africa.growtogether.platform.eaif;

import africa.growtogether.platform.eaif.governance.policy.AiGovernancePolicyService;
import africa.growtogether.platform.eaif.integration.EaifAuditRecorder;
import africa.growtogether.platform.eaif.integration.EaifPlatformIntegrationGateway;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AiFoundationService {

    private final AiProviderRepository providers;
    private final AiModelRepository models;
    private final PromptTemplateRepository prompts;
    private final AiRequestRepository requests;
    private final AiSafetyPolicy safety;
    private final EaifAuditRecorder audit;
    private final EaifPlatformIntegrationGateway platform;
    private final AiGovernancePolicyService governance;


    public AiFoundationService(
            AiProviderRepository providers,
            AiModelRepository models,
            PromptTemplateRepository prompts,
            AiRequestRepository requests,
            AiSafetyPolicy safety,
            EaifAuditRecorder audit,
            EaifPlatformIntegrationGateway platform,
            AiGovernancePolicyService governance
    ) {
        this.providers = providers;
        this.models = models;
        this.prompts = prompts;
        this.requests = requests;
        this.safety = safety;
        this.audit = audit;
        this.platform = platform;
        this.governance = governance;
    }


    public AiProvider createProvider(
            UUID tenantId,
            CreateProviderRequest q
    ) {
        if (providers.findByTenantIdAndCode(
                tenantId,
                q.code().trim().toUpperCase()
        ).isPresent()) {
            throw new IllegalStateException(
                    "Provider code already exists"
            );
        }

        return providers.save(
                new AiProvider(
                        tenantId,
                        q.code(),
                        q.name(),
                        q.type()
                )
        );
    }


    public AiModel createModel(
            UUID tenantId,
            CreateModelRequest q
    ) {
        providers.findByTenantIdAndCode(
                tenantId,
                q.providerCode().trim().toUpperCase()
        ).orElseThrow(
                () -> new IllegalArgumentException(
                        "Provider not found"
                )
        );

        return models.save(
                new AiModel(
                        tenantId,
                        q.code(),
                        q.providerCode(),
                        q.providerModel(),
                        q.capability()
                )
        );
    }


    public PromptTemplate createPrompt(
            UUID tenantId,
            CreatePromptRequest q
    ) {
        return prompts.save(
                new PromptTemplate(
                        tenantId,
                        q.code(),
                        q.version(),
                        q.userTemplate()
                )
        );
    }


    public AiRequest submit(
            UUID tenantId,
            CreateAiRequest q
    ) {

        models.findByTenantIdAndCode(
                tenantId,
                q.modelCode().trim().toUpperCase()
        ).orElseThrow(
                () -> new IllegalArgumentException(
                        "Model not found"
                )
        );


        AiRequest request = new AiRequest(
                tenantId,
                q.sourceService(),
                q.useCase(),
                q.modelCode(),
                q.inputHash(),
                q.riskLevel(),
                q.correlationId()
        );


        safety.validate(request);


        boolean allowed = governance.allows(
                tenantId,
                "DEFAULT_AI_POLICY",
                request.riskLevel()
        );


        if (!allowed) {
            throw new IllegalStateException(
                    "AI request rejected by governance policy"
            );
        }


        request.approve();

        request = requests.save(request);


        audit.success(
                "EAIF.REQUEST.SUBMITTED",
                request.getId().toString(),
                "AI request submitted",
                Map.of(
                        "modelCode", request.modelCode(),
                        "riskLevel", request.riskLevel().name(),
                        "sourceService", request.sourceService()
                )
        );


        platform.publishAnalytics(
                request.getId(),
                request.requestStatus().name(),
                request.modelCode(),
                request.riskLevel().name()
        );


        return request;
    }
    public AiRequest begin(
            UUID tenantId,
            UUID id
    ) {

        AiRequest request = get(
                tenantId,
                id
        );

        request.begin();

        audit.success(
                "EAIF.REQUEST.PROCESSING",
                id.toString(),
                "AI request processing started",
                Map.of(
                        "modelCode",
                        request.modelCode()
                )
        );

        platform.publishAnalytics(
                id,
                request.requestStatus().name(),
                request.modelCode(),
                request.riskLevel().name()
        );

        return request;
    }


    public AiRequest succeed(
            UUID tenantId,
            UUID id,
            String outputReference
    ) {

        AiRequest request = get(
                tenantId,
                id
        );

        request.succeed(outputReference);

        audit.success(
                "EAIF.REQUEST.SUCCEEDED",
                id.toString(),
                "AI request succeeded",
                Map.of(
                        "modelCode",
                        request.modelCode(),
                        "outputReferencePresent",
                        outputReference != null
                                && !outputReference.isBlank()
                )
        );

        platform.publishAnalytics(
                id,
                request.requestStatus().name(),
                request.modelCode(),
                request.riskLevel().name()
        );

        return request;
    }


    public AiRequest fail(
            UUID tenantId,
            UUID id,
            String reason
    ) {

        AiRequest request = get(
                tenantId,
                id
        );

        request.fail(reason);

        audit.failure(
                "EAIF.REQUEST.FAILED",
                id.toString(),
                "AI request failed",
                Map.of(
                        "modelCode",
                        request.modelCode(),
                        "reasonCode",
                        "PROVIDER_OR_RUNTIME_FAILURE"
                )
        );

        platform.publishAnalytics(
                id,
                request.requestStatus().name(),
                request.modelCode(),
                request.riskLevel().name()
        );

        return request;
    }


    @Transactional(readOnly = true)
    public AiRequest get(
            UUID tenantId,
            UUID id
    ) {

        return requests
                .findByIdAndTenantId(
                        id,
                        tenantId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "AI request not found"
                        )
                );
    }
}
    
