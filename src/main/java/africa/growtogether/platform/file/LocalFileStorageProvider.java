package africa.growtogether.platform.file;


import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;



@Service
public class LocalFileStorageProvider
        implements FileStorageProvider {


    private final Path root =
            Paths.get(
                    "storage/uploads"
            );



    public LocalFileStorageProvider() {

        try {

            Files.createDirectories(
                    root
            );

        }
        catch (IOException e) {

            throw new IllegalStateException(
                    "Unable to initialise storage",
                    e
            );

        }

    }



    @Override
    public String store(
            UUID tenantId,
            MultipartFile file
    ) {


        try {


            Path tenantRoot =
                    root.resolve(
                            tenantId.toString()
                    );


            Files.createDirectories(
                    tenantRoot
            );


            String filename =
                    UUID.randomUUID()
                    + "-"
                    + file.getOriginalFilename();



            Path target =
                    tenantRoot.resolve(
                            filename
                    );


            Files.copy(
                    file.getInputStream(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );


            return target.toString();


        }
        catch (IOException e) {

            throw new IllegalStateException(
                    "Unable to store file",
                    e
            );

        }

    }



    @Override
    public Resource load(
            String storageKey
    ) {


        try {

            Path file =
                    Paths.get(
                            storageKey
                    )
                    .normalize();



            if (
                    !Files.exists(file)
            ) {

                throw new IllegalArgumentException(
                        "File not found"
                );

            }



            return new UrlResource(
                    file.toUri()
            );


        }
        catch (
                MalformedURLException e
        ) {

            throw new IllegalStateException(
                    "Unable to load file",
                    e
            );

        }

    }

}
