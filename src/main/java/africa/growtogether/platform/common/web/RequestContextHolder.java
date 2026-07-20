package africa.growtogether.platform.common.web;

import java.util.Optional;

public final class RequestContextHolder {
    private static final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<>();

    private RequestContextHolder() {}

    public static void set(RequestContext context) {
        CONTEXT.set(context);
    }

    public static RequestContext require() {
        RequestContext context = CONTEXT.get();
        if (context == null) {
            throw new IllegalStateException("Request context is not available");
        }
        return context;
    }

    public static Optional<RequestContext> current() {
        return Optional.ofNullable(CONTEXT.get());
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
