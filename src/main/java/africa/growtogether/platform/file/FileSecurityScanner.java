package africa.growtogether.platform.file;


import org.springframework.web.multipart.MultipartFile;



public interface FileSecurityScanner {


    FileSecurityScanResult scan(
            MultipartFile file
    );


}
