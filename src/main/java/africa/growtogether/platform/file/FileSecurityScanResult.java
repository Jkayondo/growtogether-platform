package africa.growtogether.platform.file;


public record FileSecurityScanResult(

        Status status,

        String message

) {


    public enum Status {

        CLEAN,

        QUARANTINED,

        PENDING_SCAN

    }


    public static FileSecurityScanResult clean() {

        return new FileSecurityScanResult(
                Status.CLEAN,
                "File passed security scan"
        );

    }


}
