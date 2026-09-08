package africa.growtogether.platform.file;


import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/v1/files/access")
public class FileSignedDownloadController {


    private final FileAccessService access;

    private final FileStorageProvider storage;



    public FileSignedDownloadController(
            FileAccessService access,
            FileStorageProvider storage
    ) {

        this.access = access;
        this.storage = storage;

    }



    @GetMapping("/{token}")
    public ResponseEntity<Resource> download(
            @PathVariable String token
    ) {


        FileRecord file =
                access.resolveFile(
                        token
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
