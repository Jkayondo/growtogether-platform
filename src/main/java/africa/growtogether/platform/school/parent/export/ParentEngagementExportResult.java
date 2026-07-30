package africa.growtogether.platform.school.parent.export;


public class ParentEngagementExportResult {


    private final ParentEngagementExportFormat format;

    private final String fileName;

    private final String content;


    public ParentEngagementExportResult(
            ParentEngagementExportFormat format,
            String fileName,
            String content
    ) {

        this.format = format;
        this.fileName = fileName;
        this.content = content;
    }


    public ParentEngagementExportFormat getFormat() {
        return format;
    }


    public String getFileName() {
        return fileName;
    }


    public String getContent() {
        return content;
    }
}
