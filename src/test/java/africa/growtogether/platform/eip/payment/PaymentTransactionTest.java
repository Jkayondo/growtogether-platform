package africa.growtogether.platform.eip.payment;
import static org.junit.jupiter.api.Assertions.*; import java.math.BigDecimal; import java.util.UUID; import org.junit.jupiter.api.Test;
class PaymentTransactionTest {private PaymentTransaction payment(){return new PaymentTransaction(UUID.randomUUID(),"INV-1","256700000000",new BigDecimal("10000"),"UGX",PaymentChannel.MOBILE_MONEY,"MTN_MOMO","idem-1");}
 @Test void succeedsAfterSubmission(){var p=payment();p.submit();p.succeed("provider-1");assertEquals(PaymentTransactionStatus.SUCCEEDED,p.status());}
 @Test void duplicateTerminalTransitionRejected(){var p=payment();p.submit();p.succeed("provider-1");assertThrows(IllegalStateException.class,()->p.fail("X","late"));}
 @Test void cancellationOnlyBeforeCompletion(){var p=payment();p.submit();p.cancel();assertEquals(PaymentTransactionStatus.CANCELLED,p.status());}
 @Test void amountMustBePositive(){assertThrows(IllegalArgumentException.class,()->new PaymentTransaction(UUID.randomUUID(),"INV","payer",BigDecimal.ZERO,"UGX",PaymentChannel.BANK_TRANSFER,"BANK","x"));}}
