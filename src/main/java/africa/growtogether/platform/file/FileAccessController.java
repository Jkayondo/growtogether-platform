package africa.growtogether.platform.file;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;



@RestController
@RequestMapping("/api/v1/files")
public class FileAccessController {


    private final FileAccessService service;



    public FileAccessController(
            FileAccessService service
    ) {

        this.service = service;

    }



    @PostMapping("/{id}/access")
    @PreAuthorize(
            "hasAuthority('file.access.create')"
    )
    public ResponseEntity<AccessResponse> create(
            @PathVariable UUID id
    ) {


        String token =
                service.create(
                        id
                );


        return ResponseEntity.ok(
                new AccessResponse(
                        token,
                        24
                )
        );

    }



    public record AccessResponse(
            String accessToken,
            int expiresInHours
    ) {}

}
