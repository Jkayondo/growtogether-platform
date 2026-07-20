package africa.growtogether.platform.common.api;

import java.util.List;

public record ApiResponse<T>(
    boolean success,
    String code,
    String message,
    T data,
    List<FieldViolation> errors,
    ApiMetadata metadata
) {
    public static <T> ApiResponse<T> success(
        String code,
        String message,
        T data,
        ApiMetadata metadata
    ) {
        return new ApiResponse<>(true, code, message, data, List.of(), metadata);
    }

    public static ApiResponse<Void> failure(
        String code,
        String message,
        List<FieldViolation> errors,
        ApiMetadata metadata
    ) {
        return new ApiResponse<>(false, code, message, null, List.copyOf(errors), metadata);
    }
}
