package africa.growtogether.platform.file;


import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;



@RestController
@RequestMapping("/api/v1/files")
public class FileDownloadController {


    private final FileDownloadService downloads;

    private final FileStorageProvider storage;



    public FileDownloadController(
            FileDownloadService downloads,
            FileStorageProvider storage
    ) {

        this.downloads = downloads;
        this.storage = storage;

    }



    @GetMapping("/{id}/download")
    @PreAuthorize(
            "hasAuthority('file.download')"
    )
    public ResponseEntity<Resource> download(
            @PathVariable UUID id
    ) {


        FileRecord file =
                downloads.authorize(
                        id
                );


        Resource resource =
                storage.load(
                        file.storageKey()
                );


        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                file.mimeType()
                        )
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment"
                )
                .body(
                        resource
                );

    }

}
