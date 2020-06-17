package textsVocal.web.uploadingfiles;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * copy-past from Spring examples https://spring.io/guides/gs/uploading-files/
 */
@ConfigurationProperties("storage")
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    private String inputLocation = "upload-dir";
    private String outputLocation = "saveResults-dir";

    public String getInputLocation() {
        return inputLocation;
    }

    public void setInputLocation(String location) {
        this.inputLocation = location;
    }

    //Myropolskyi
    public String getOutputLocation() {
        return outputLocation;
    }

    public void setOutputLocation(String outputLocation) {
        this.outputLocation = outputLocation;
    }
}
