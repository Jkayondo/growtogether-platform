package africa.growtogether.platform.file;


import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;


import java.util.UUID;



public interface FileStorageProvider {


    String store(
            UUID tenantId,
            MultipartFile file
    );


    Resource load(
            String storageKey
    );

}
