package africa.growtogether.platform.eaif;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*; import java.time.Instant;
@Entity @Table(name="eaif_requests", indexes={@Index(name="ix_eaif_request_status",columnList="tenant_id,request_status,created_at"),@Index(name="ix_eaif_request_source",columnList="tenant_id,source_service,correlation_id")})
public class AiRequest extends AuditedTenantEntity {
 @Column(name="source_service",nullable=false,length=100) private String sourceService;
 @Column(name="use_case",nullable=false,length=160) private String useCase;
 @Column(name="model_code",nullable=false,length=100) private String modelCode;
 @Column(name="prompt_template_code",length=100) private String promptTemplateCode;
 @Column(name="input_reference",length=500) private String inputReference;
 @Column(name="input_hash",nullable=false,length=64) private String inputHash;
 @Enumerated(EnumType.STRING) @Column(name="risk_level",nullable=false,length=20) private AiEnums.RiskLevel riskLevel;
 @Enumerated(EnumType.STRING) @Column(name="request_status",nullable=false,length=30) private AiEnums.RequestStatus requestStatus=AiEnums.RequestStatus.RECEIVED;
 @Column(name="correlation_id",length=100) private String correlationId;
 @Column(name="output_reference",length=500) private String outputReference;
 @Column(name="failure_reason",columnDefinition="text") private String failureReason;
 @Column(name="completed_at") private Instant completedAt;
 protected AiRequest(){}
 public AiRequest(java.util.UUID tenantId,String source,String useCase,String model,String inputHash,AiEnums.RiskLevel risk,String correlationId){setTenantId(tenantId);this.sourceService=req(source);this.useCase=req(useCase);this.modelCode=req(model).toUpperCase();this.inputHash=req(inputHash);this.riskLevel=java.util.Objects.requireNonNull(risk);this.correlationId=correlationId;}
 private static String req(String s){if(s==null||s.isBlank())throw new IllegalArgumentException("value is required");return s.trim();}
 public void approve(){if(requestStatus!=AiEnums.RequestStatus.RECEIVED&&requestStatus!=AiEnums.RequestStatus.VALIDATING)throw new IllegalStateException("Request cannot be approved");requestStatus=AiEnums.RequestStatus.APPROVED;}
 public void begin(){if(requestStatus!=AiEnums.RequestStatus.APPROVED)throw new IllegalStateException("Request is not approved");requestStatus=AiEnums.RequestStatus.PROCESSING;}
 public void succeed(String outputReference){if(requestStatus!=AiEnums.RequestStatus.PROCESSING)throw new IllegalStateException("Request is not processing");this.outputReference=req(outputReference);requestStatus=AiEnums.RequestStatus.SUCCEEDED;completedAt=Instant.now();}
 public void fail(String reason){if(requestStatus!=AiEnums.RequestStatus.PROCESSING)throw new IllegalStateException("Request is not processing");failureReason=req(reason);requestStatus=AiEnums.RequestStatus.FAILED;completedAt=Instant.now();}
 public void reject(String reason){if(requestStatus==AiEnums.RequestStatus.SUCCEEDED||requestStatus==AiEnums.RequestStatus.CANCELLED)throw new IllegalStateException("Request is terminal");failureReason=req(reason);requestStatus=AiEnums.RequestStatus.REJECTED;completedAt=Instant.now();}
 public AiEnums.RequestStatus requestStatus(){return requestStatus;} public String useCase(){return useCase;} public String modelCode(){return modelCode;} public AiEnums.RiskLevel riskLevel(){return riskLevel;} public String sourceService(){return sourceService;} public String correlationId(){return correlationId;} public String outputReference(){return outputReference;}
}
