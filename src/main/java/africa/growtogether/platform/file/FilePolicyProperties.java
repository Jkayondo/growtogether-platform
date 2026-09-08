package africa.growtogether.platform.file;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


import java.util.ArrayList;
import java.util.List;



@Configuration
@ConfigurationProperties(prefix = "gt.file")
public class FilePolicyProperties {


    private long maxSizeBytes =
            10485760;


    private List<String> allowedTypes =
            new ArrayList<>();



    public long getMaxSizeBytes() {

        return maxSizeBytes;

    }



    public void setMaxSizeBytes(
            long maxSizeBytes
    ) {

        this.maxSizeBytes = maxSizeBytes;

    }



    public List<String> getAllowedTypes() {

        return allowedTypes;

    }



    public void setAllowedTypes(
            List<String> allowedTypes
    ) {

        this.allowedTypes = allowedTypes;

    }

}
