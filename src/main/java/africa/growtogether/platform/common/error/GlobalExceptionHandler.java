package africa.growtogether.platform.common.error;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.api.FieldViolation;
import africa.growtogether.platform.common.web.InvalidRequestHeaderException;
import africa.growtogether.platform.eiam.user.DuplicateUserException;
import africa.growtogether.platform.eiam.user.UserNotFoundException;
import africa.growtogether.platform.eiam.user.UserLifecycleException;
import africa.growtogether.platform.eiam.role.DuplicateRoleException;
import africa.growtogether.platform.eiam.role.RoleLifecycleException;
import africa.growtogether.platform.eiam.role.RoleNotFoundException;
import africa.growtogether.platform.eiam.permission.DuplicatePermissionException;
import africa.growtogether.platform.eiam.permission.PermissionLifecycleException;
import africa.growtogether.platform.eiam.permission.PermissionNotFoundException;
import africa.growtogether.platform.eiam.auth.AuthenticationException;
import africa.growtogether.platform.eiam.recovery.InvalidRecoveryTokenException;
import africa.growtogether.platform.eiam.mfa.MfaException;
import africa.growtogether.platform.eiam.tenant.TenantLifecycleException;
import africa.growtogether.platform.eiam.authorization.AuthorizationPolicyException;
import africa.growtogether.platform.eiam.membership.MembershipException;
import africa.growtogether.platform.ecs.ConfigurationException;
import africa.growtogether.platform.ewe.WorkflowException;
import jakarta.validation.ConstraintViolationException;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public final class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ApiResponses responses;

    public GlobalExceptionHandler(ApiResponses responses) {
        this.responses = responses;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handleBodyValidation(MethodArgumentNotValidException exception) {
        List<FieldViolation> violations = exception.getBindingResult().getFieldErrors().stream()
            .map(error -> new FieldViolation(error.getField(), error.getDefaultMessage()))
            .sorted(Comparator.comparing(FieldViolation::field))
            .toList();
        return ResponseEntity.badRequest().body(
            responses.failure("GT-VALIDATION-001", "Request validation failed.", violations)
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiResponse<Void>> handleConstraintValidation(ConstraintViolationException exception) {
        List<FieldViolation> violations = exception.getConstraintViolations().stream()
            .map(violation -> new FieldViolation(
                violation.getPropertyPath().toString(),
                violation.getMessage()
            ))
            .sorted(Comparator.comparing(FieldViolation::field))
            .toList();
        return ResponseEntity.badRequest().body(
            responses.failure("GT-VALIDATION-002", "Request validation failed.", violations)
        );
    }

    @ExceptionHandler(InvalidRequestHeaderException.class)
    ResponseEntity<ApiResponse<Void>> handleInvalidHeader(InvalidRequestHeaderException exception) {
        return ResponseEntity.badRequest().body(
            responses.failure(
                "GT-REQUEST-001",
                "Invalid request header.",
                List.of(new FieldViolation(exception.headerName(), exception.getMessage()))
            )
        );
    }


    @ExceptionHandler(DuplicateUserException.class)
    ResponseEntity<ApiResponse<Void>> handleDuplicateUser(DuplicateUserException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            responses.failure("GT-EIAM-USER-409", "User account conflicts with an existing account.",
                List.of(new FieldViolation(exception.field(), exception.getMessage())))
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            responses.failure("GT-EIAM-USER-404", exception.getMessage(), List.of())
        );
    }

    @ExceptionHandler(UserLifecycleException.class)
    ResponseEntity<ApiResponse<Void>> handleUserLifecycle(UserLifecycleException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            responses.failure("GT-EIAM-USER-STATE-409", exception.getMessage(),
                List.of(new FieldViolation("accountStatus",
                    "Cannot " + exception.action().name().toLowerCase() +
                    " a user in state " + exception.currentStatus().name() + ".")))
        );
    }

    @ExceptionHandler(DuplicateRoleException.class)
    ResponseEntity<ApiResponse<Void>> handleDuplicateRole(DuplicateRoleException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            responses.failure("GT-EIAM-ROLE-409", "Role conflicts with an existing role.",
                List.of(new FieldViolation(exception.field(), exception.getMessage())))
        );
    }

    @ExceptionHandler(RoleNotFoundException.class)
    ResponseEntity<ApiResponse<Void>> handleRoleNotFound(RoleNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            responses.failure("GT-EIAM-ROLE-404", exception.getMessage(), List.of())
        );
    }

    @ExceptionHandler(RoleLifecycleException.class)
    ResponseEntity<ApiResponse<Void>> handleRoleLifecycle(RoleLifecycleException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            responses.failure("GT-EIAM-ROLE-STATE-409", exception.getMessage(), List.of())
        );
    }


    @ExceptionHandler(DuplicatePermissionException.class)
    ResponseEntity<ApiResponse<Void>> handleDuplicatePermission(DuplicatePermissionException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            responses.failure("GT-EIAM-PERM-409", exception.getMessage(),
                List.of(new FieldViolation("code", exception.getMessage())))
        );
    }

    @ExceptionHandler(PermissionNotFoundException.class)
    ResponseEntity<ApiResponse<Void>> handlePermissionNotFound(PermissionNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            responses.failure("GT-EIAM-PERM-404", exception.getMessage(), List.of())
        );
    }

    @ExceptionHandler(PermissionLifecycleException.class)
    ResponseEntity<ApiResponse<Void>> handlePermissionLifecycle(PermissionLifecycleException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            responses.failure("GT-EIAM-PERM-STATE-409", exception.getMessage(), List.of())
        );
    }


    @ExceptionHandler(InvalidRecoveryTokenException.class)
    ResponseEntity<ApiResponse<Void>> handleRecoveryToken(InvalidRecoveryTokenException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            responses.failure("GT-EIAM-RECOVERY-401", exception.getMessage(), List.of())
        );
    }


    @ExceptionHandler(MfaException.class)
    ResponseEntity<ApiResponse<Void>> handleMfa(MfaException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            responses.failure("GT-EIAM-MFA-401", exception.getMessage(), List.of())
        );
    }

    @ExceptionHandler(TenantLifecycleException.class)
    ResponseEntity<ApiResponse<Void>> handleTenantLifecycle(TenantLifecycleException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            responses.failure("GT-EIAM-TENANT-409", exception.getMessage(), List.of())
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            responses.failure("GT-EIAM-AUTH-401", exception.getMessage(), List.of())
        );
    }


    @ExceptionHandler(AuthorizationPolicyException.class)
    ResponseEntity<ApiResponse<Void>> handleAuthorizationPolicy(AuthorizationPolicyException exception) {
        HttpStatus status = exception.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
        return ResponseEntity.status(status).body(
            responses.failure("GT-EIAM-AUTHZ-409", exception.getMessage(), List.of())
        );
    }


    @ExceptionHandler(MembershipException.class)
    ResponseEntity<ApiResponse<Void>> handleMembership(MembershipException exception) {
        HttpStatus status = exception.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
        return ResponseEntity.status(status).body(
            responses.failure("GT-EIAM-MEMBERSHIP-409", exception.getMessage(), List.of())
        );
    }

    @ExceptionHandler(WorkflowException.class)
    ResponseEntity<ApiResponse<Void>> handleWorkflow(WorkflowException exception) {
        HttpStatus status = exception.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
        return ResponseEntity.status(status).body(
            responses.failure("GT-EWE-409", exception.getMessage(), List.of())
        );
    }

    @ExceptionHandler(ConfigurationException.class)
    ResponseEntity<ApiResponse<Void>> handleConfiguration(ConfigurationException exception) {
        HttpStatus status = exception.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
        return ResponseEntity.status(status).body(
            responses.failure("GT-ECS-409", exception.getMessage(), List.of())
        );
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception) {
        LOGGER.error("Unhandled request failure", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            responses.failure("GT-SYSTEM-001", "An unexpected error occurred.", List.of())
        );
    }
}
