package textsVocal.web.uploadingfiles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import textsVocal.config.CommonConstants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * copy-past from Spring examples https://spring.io/guides/gs/uploading-files/
 */
@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;
    private final Path outputLocation;//Myropolskyi

    private static Logger log = LoggerFactory.getLogger(FileSystemStorageService.class);

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getInputLocation());
        //Myropolskyi
        this.outputLocation = Paths.get(properties.getOutputLocation());
    }

    @Override
    public void store(MultipartFile file) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (file == null || file.isEmpty()) {
                log.error("Failed to store empty file {}", filename);
                throw new StorageException("Failed to store empty file " + filename);
            }
            if (!file.getContentType().trim().equals("text/plain")) {//Myropolskyi
                log.error("Failed to store not *.txt file  {}", filename);
                throw new StorageException("Failed to store not *.txt file " + filename);
            }
            if (filename.contains("..")) {
                // This is a security check
                log.error("Cannot store file with relative path outside current directory {}", filename);
                throw new StorageException(
                        "Cannot store file with relative path outside current directory "
                                + filename);
            }
            try (InputStream inputStream = file.getInputStream()) {
                log.info("Storing file {}", file.getOriginalFilename());
                //Myropolskyi
       /*         Files.copy(inputStream, this.rootLocation.resolve(filename),
                        StandardCopyOption.REPLACE_EXISTING);*/ //I don't know? why in local host this copyes file to itself
                String simpleFilename = new File(filename).getName();
                Files.copy(inputStream, Paths.get(this.rootLocation.toString(), simpleFilename),
                        StandardCopyOption.REPLACE_EXISTING);
                //Myropolskyi
                setFileAttributesToCommonConstants(simpleFilename, true);
            }
        } catch (IOException e) {
            log.error("Failed to store file {}", filename);
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        } catch (IOException e) {
            log.error("Failed to store file {}", e.getMessage());
            throw new StorageException("Failed to read stored files", e);
        }
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                log.error("Could not read file: {}", filename);
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);

            }
        } catch (MalformedURLException e) {
            log.error("Could not read file: {}", filename);
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        // FileSystemUtils.deleteRecursively(rootLocation.toFile());

        //Myropolskyi: delete files older then today, not all the files
        LocalDateTime currentDate = LocalDateTime.now(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS);
        BasicFileAttributes attributes = null;

        //input directory
        File rootDir = new File(rootLocation.toAbsolutePath().toString());
        File[] filesRootDir = null;
        if (rootDir.exists()) {
            filesRootDir = rootDir.listFiles();
        }
        if (filesRootDir == null) {
            filesRootDir = new File[0];
        }
        for (File file : filesRootDir) {
            try {
                attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                Instant stampCreation = attributes.creationTime().toInstant();
                LocalDateTime creationDate = LocalDateTime.ofInstant(stampCreation, ZoneId.systemDefault());
                if (currentDate.isAfter(creationDate) && file.isFile()) {
                    file.delete();
                }
            } catch (IOException exception) {
                log.info("Exception handled when trying to get input file attributes: {}", exception.getMessage());
            } catch (NullPointerException e) {
                log.info("Something wrong with files deleting: {}", e.getMessage());
            }
        }

        //output directrory
        File outputDir = new File(outputLocation.toAbsolutePath().toString());
        File[] filesOutputDir = null;
        if (outputDir.exists()) {
            filesOutputDir = outputDir.listFiles();
        }
        if (filesOutputDir == null) {
            filesOutputDir = new File[0];
        }
        for (File file : filesOutputDir) {
            try {
                attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                Instant stampCreation = attributes.creationTime().toInstant();
                LocalDateTime creationDate = LocalDateTime.ofInstant(stampCreation, ZoneId.systemDefault());
                if (currentDate.isAfter(creationDate) && file.isFile()) {
                    file.delete();
                }
            } catch (IOException exception) {
                log.info("Exception handled when trying to get output file attributes: {}", exception.getMessage());
            } catch (NullPointerException e) {
                log.info("Something wrong with files deleting: {}", e.getMessage());
            }
        }
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            Files.createDirectories(outputLocation);
        } catch (IOException e) {
            log.error("Could not initialize storage: {}", e.getMessage());
            throw new StorageException("Could not initialize storage", e);
        }
    }

    //Myropolskyi
    public void setFileAttributesToCommonConstants(String filename, boolean readingFromFile) {
        CommonConstants commonConstants = CommonConstants.getApplicationContext().getBean(CommonConstants.class);
        commonConstants.setFileInputDirectory(Paths.get(this.rootLocation.toString()).toAbsolutePath().toString());
        commonConstants.setFileInputName(filename);
        commonConstants.setFileOutputDirectory(Paths.get(this.outputLocation.toString()).toAbsolutePath().toString());
        commonConstants.setFileOutputName("out_" + filename);
        commonConstants.setReadingFromFile(readingFromFile);
    }


    //Myropolskyi
    public Stream<Path> loadAllOutput() {
        try {
            return Files.walk(this.outputLocation, 1)
                    .filter(path -> !path.equals(this.outputLocation))
                    .map(this.outputLocation::relativize);
        } catch (IOException e) {
            log.error("Failed to read stored files: {}", e.getMessage());
            throw new StorageException("Failed to read stored files", e);
        }
    }

    //Myropolskyi
    public Path loadOutput(String filename) {
        return outputLocation.resolve(filename);
    }

    //Myropolskyi
    public Resource loadAsResourceOutput(String filename) {
        try {
            Path file = loadOutput(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                log.error("Could not read file: {}", filename);
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            log.error("Could not read file: {}", filename);
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

}
