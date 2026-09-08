package africa.growtogether.platform.file;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;



@Service
public class DefaultFileSecurityScanner
        implements FileSecurityScanner {


    @Override
    public FileSecurityScanResult scan(
            MultipartFile file
    ) {


        /*
         * Placeholder integration point.
         *
         * Future implementations may connect:
         * - ClamAV
         * - enterprise malware scanners
         * - cloud security services
         * - AI document threat analysis
         */


        return FileSecurityScanResult.clean();

    }

}
