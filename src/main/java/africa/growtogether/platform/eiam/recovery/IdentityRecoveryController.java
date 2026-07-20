package africa.growtogether.platform.eiam.recovery;

import africa.growtogether.platform.common.api.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/eiam")
public class IdentityRecoveryController {
    private final IdentityRecoveryService service; private final ApiResponses responses;
    public IdentityRecoveryController(IdentityRecoveryService service,ApiResponses responses){this.service=service;this.responses=responses;}
    @PostMapping("/auth/password-reset/request")
    public ResponseEntity<ApiResponse<RecoveryDispatch>> requestReset(@Valid @RequestBody PasswordResetRequest request){var dispatch=service.requestPasswordReset(request).orElse(null); return ResponseEntity.ok(responses.success("GT-EIAM-RESET-REQUEST-200","If the account exists, recovery instructions have been issued.",dispatch));}
    @PostMapping("/auth/password-reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmReset(@Valid @RequestBody PasswordResetConfirm request){service.confirmPasswordReset(request);return ResponseEntity.ok(responses.success("GT-EIAM-RESET-CONFIRM-200","Password reset completed.",null));}
    @PostMapping("/email-verification/request")
    public ResponseEntity<ApiResponse<RecoveryDispatch>> requestVerification(@Valid @RequestBody EmailVerificationRequest request){var dispatch=service.requestEmailVerification(request).orElse(null);return ResponseEntity.ok(responses.success("GT-EIAM-EMAIL-REQUEST-200","If verification is required, instructions have been issued.",dispatch));}
    @PostMapping("/email-verification/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmVerification(@Valid @RequestBody TokenConfirmation request){service.confirmEmailVerification(request);return ResponseEntity.ok(responses.success("GT-EIAM-EMAIL-CONFIRM-200","Email address verified.",null));}
    @PostMapping("/auth/account-recovery/request")
    public ResponseEntity<ApiResponse<RecoveryDispatch>> requestRecovery(@Valid @RequestBody AccountRecoveryRequest request){var dispatch=service.requestAccountRecovery(request).orElse(null);return ResponseEntity.ok(responses.success("GT-EIAM-RECOVERY-REQUEST-200","If the account exists, recovery instructions have been issued.",dispatch));}
    @PostMapping("/auth/account-recovery/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmRecovery(@Valid @RequestBody PasswordResetConfirm request){service.confirmAccountRecovery(request);return ResponseEntity.ok(responses.success("GT-EIAM-RECOVERY-CONFIRM-200","Account recovery completed.",null));}
}
