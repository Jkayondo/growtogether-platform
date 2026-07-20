package africa.growtogether.platform.eiam.auth;
import africa.growtogether.platform.common.api.*; import jakarta.validation.Valid; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/eiam/auth") public class AuthenticationController {private final AuthenticationService service;private final ApiResponses responses; public AuthenticationController(AuthenticationService service,ApiResponses responses){this.service=service;this.responses=responses;}
 @PostMapping("/login") public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginCommand c){LoginResponse result=service.login(c);return ResponseEntity.ok(responses.success(result.mfaRequired()?"GT-EIAM-AUTH-MFA-202":"GT-EIAM-AUTH-LOGIN-200",result.mfaRequired()?"MFA verification is required.":"Authentication succeeded.",result));}
 @PostMapping("/mfa") public ResponseEntity<ApiResponse<LoginResponse>> mfa(@Valid @RequestBody MfaLoginCommand c){return ResponseEntity.ok(responses.success("GT-EIAM-AUTH-MFA-200","MFA verification succeeded.",service.completeMfa(c)));}
 @PostMapping("/refresh") public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshCommand c){return ResponseEntity.ok(responses.success("GT-EIAM-AUTH-REFRESH-200","Session refreshed.",service.refresh(c)));}
 @PostMapping("/logout") public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutCommand c){service.logout(c);return ResponseEntity.ok(responses.success("GT-EIAM-AUTH-LOGOUT-200","Session revoked.",null));}
}
