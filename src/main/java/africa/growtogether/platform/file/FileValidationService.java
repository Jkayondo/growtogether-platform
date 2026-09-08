package africa.growtogether.platform.file;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;



@Service
public class FileValidationService {


    private final FilePolicyProperties policy;



    public FileValidationService(
            FilePolicyProperties policy
    ) {
        this.policy = policy;
    }



    public void validate(
            MultipartFile file
    ) {


        if (file == null) {

            throw new IllegalArgumentException(
                    "File must not be null"
            );

        }



        if (file.isEmpty()) {

            throw new IllegalArgumentException(
                    "File must not be empty"
            );

        }



        if (
                file.getSize()
                >
                policy.getMaxSizeBytes()
        ) {

            throw new IllegalArgumentException(
                    "File exceeds maximum allowed size"
            );

        }



        validateMimeType(
                file
        );


        validateFilename(
                file
        );

    }



    private void validateMimeType(
            MultipartFile file
    ) {


        String mimeType =
                file.getContentType();



        if (
                mimeType == null
                ||
                mimeType.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "File MIME type is required"
            );

        }



        if (
                !policy.getAllowedTypes()
                        .contains(mimeType)
        ) {

            throw new IllegalArgumentException(
                    "Unsupported file type"
            );

        }

    }



    private void validateFilename(
            MultipartFile file
    ) {


        String name =
                file.getOriginalFilename();



        if (
                name == null
                ||
                name.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Filename is required"
            );

        }



        String lower =
                name.toLowerCase();



        if (
                lower.contains("..")
                ||
                lower.contains("/")
                ||
                lower.contains("\\")
        ) {

            throw new IllegalArgumentException(
                    "Invalid filename"
            );

        }

    }

}
