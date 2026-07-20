package africa.growtogether.platform.eiam.mfa;
import africa.growtogether.platform.common.api.*; import jakarta.validation.Valid; import java.util.*; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/eiam/mfa") public class MfaController {private final MfaService service;private final ApiResponses responses;public MfaController(MfaService service,ApiResponses responses){this.service=service;this.responses=responses;}
 @PostMapping("/enroll") public ResponseEntity<ApiResponse<EnrollmentResponse>> enroll(){return ResponseEntity.ok(responses.success("GT-EIAM-MFA-ENROLL-200","MFA enrollment created.",service.enroll()));}
 @PostMapping("/verify") public ResponseEntity<ApiResponse<Void>> verify(@Valid @RequestBody VerifyEnrollmentCommand c){service.verifyEnrollment(c);return ResponseEntity.ok(responses.success("GT-EIAM-MFA-VERIFY-200","MFA enabled.",null));}
 @PostMapping("/disable") public ResponseEntity<ApiResponse<Void>> disable(@Valid @RequestBody DisableMfaCommand c){service.disable(c);return ResponseEntity.ok(responses.success("GT-EIAM-MFA-DISABLE-200","MFA disabled and active sessions revoked.",null));}
 @GetMapping("/trusted-devices") public ResponseEntity<ApiResponse<List<TrustedDeviceView>>> devices(){return ResponseEntity.ok(responses.success("GT-EIAM-DEVICE-LIST-200","Trusted devices retrieved.",service.listDevices()));}
 @DeleteMapping("/trusted-devices/{id}") public ResponseEntity<ApiResponse<Void>> revoke(@PathVariable UUID id){service.revokeDevice(id);return ResponseEntity.ok(responses.success("GT-EIAM-DEVICE-REVOKE-200","Trusted device revoked.",null));}
}
