package africa.growtogether.platform.common.web;

public final class InvalidRequestHeaderException extends RuntimeException {
    private final String headerName;

    public InvalidRequestHeaderException(String headerName, String message) {
        super(message);
        this.headerName = headerName;
    }

    public String headerName() {
        return headerName;
    }
}
