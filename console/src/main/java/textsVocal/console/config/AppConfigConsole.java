package textsVocal.console.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import textsVocal.config.AppConfigCore;
import textsVocal.config.CommonConstants;

@Configuration
@Import(AppConfigCore.class)
public class AppConfigConsole {

    @Bean("commonConstants")
    public CommonConstants commonConstants() {
        return new CommonConstants();
    }

}

