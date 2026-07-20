package africa.growtogether.platform.eip.payment;
import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
public final class PaymentDtos {private PaymentDtos(){}
 public record CreatePayment(@NotBlank String merchantReference,String payerReference,@NotNull @DecimalMin("0.0001") BigDecimal amount,@NotBlank String currency,@NotNull PaymentChannel channel,@NotBlank String connectorCode,@NotBlank String idempotencyKey){}
 public record ProviderResult(@NotBlank String providerReference,@NotBlank String responseCode,String responseMessage){}
 public record Failure(@NotBlank String code,@NotBlank String message){}
 public record SettlementCommand(@NotBlank String connectorCode,@NotBlank String providerSettlementReference,@NotNull LocalDate settlementDate,@NotNull BigDecimal grossAmount,BigDecimal feeAmount,@NotBlank String currency){}
 public record ReconcileCommand(@NotNull UUID paymentTransactionId,@NotNull UUID settlementId,@NotBlank String providerReference,@NotNull BigDecimal providerAmount){}
 public record DisputeCommand(@NotNull UUID paymentTransactionId,@NotBlank String reasonCode,@NotBlank String description){}
 public record PaymentView(UUID id,String merchantReference,BigDecimal amount,String currency,PaymentChannel channel,String connectorCode,PaymentTransactionStatus status,String providerReference,String failureCode){static PaymentView from(PaymentTransaction p){return new PaymentView(p.id(),p.merchantReference(),p.amount(),p.currency(),p.channel(),p.connectorCode(),p.status(),p.providerReference(),p.failureCode());}}
 public record SettlementView(UUID id,BigDecimal netAmount,SettlementStatus status){static SettlementView from(PaymentSettlement s){return new SettlementView(s.id(),s.netAmount(),s.status());}}
 public record ReconciliationView(UUID id,ReconciliationStatus status){static ReconciliationView from(PaymentReconciliation r){return new ReconciliationView(r.id(),r.status());}}
}
