package africa.growtogether.platform.common.api;

import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public final class ApiResponses {

    private static final String DEFAULT_SUCCESS_CODE = "SUCCESS";
    private static final String DEFAULT_SUCCESS_MESSAGE = "Request completed successfully.";

    private final Clock clock;

    public ApiResponses() {
        this(Clock.systemUTC());
    }

    ApiResponses(Clock clock) {
        this.clock = clock;
    }

    /**
     * Compatibility method used by existing controllers.
     */
    public static <T> ApiResponse<T> success(T data) {
    RequestContext context = RequestContextHolder.require();

    ApiMetadata metadata = new ApiMetadata(
        context.correlationId(),
        context.tenantId(),
        Instant.now()
    );

    return ApiResponse.success(
        DEFAULT_SUCCESS_CODE,
        DEFAULT_SUCCESS_MESSAGE,
        data,
        metadata
    );
}

    /**
     * Detailed success response for callers that provide a specific code
     * and message.
     */
    public <T> ApiResponse<T> success(
        String code,
        String message,
        T data
    ) {
        return ApiResponse.success(
            code,
            message,
            data,
            metadata()
        );
    }

    public ApiResponse<Void> failure(
        String code,
        String message,
        List<FieldViolation> errors
    ) {
        return ApiResponse.failure(
            code,
            message,
            errors,
            metadata()
        );
    }

    private ApiMetadata metadata() {
        RequestContext context = RequestContextHolder.require();

        return new ApiMetadata(
            context.correlationId(),
            context.tenantId(),
            Instant.now(clock)
        );
    }
}