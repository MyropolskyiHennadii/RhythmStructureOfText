package textsVocal.web;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import textsVocal.config.CommonConstants;
import textsVocal.web.uploadingfiles.StorageProperties;
import textsVocal.web.uploadingfiles.StorageService;

/**
 * main class webapp
 */
@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class MainRythmWebApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(MainRythmWebApplication.class, args);
        CommonConstants commonConstants = context.getBean(CommonConstants.class);
        commonConstants.setApplicationContext(context);
        commonConstants.setThisIsWebApp(true);
    }

    @Bean
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {
            storageService.deleteAll();
            storageService.init();
        };
    }

}

