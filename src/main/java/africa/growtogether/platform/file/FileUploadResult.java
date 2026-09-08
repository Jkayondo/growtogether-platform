package africa.growtogether.platform.file;


public record FileUploadResult(

        String storageKey,

        String checksum,

        String mimeType,

        long sizeBytes

) {
}
