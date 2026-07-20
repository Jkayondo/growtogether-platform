package africa.growtogether.platform.eiam.recovery;
import jakarta.validation.constraints.Email;
public record EmailVerificationRequest(@Email String email) {}
