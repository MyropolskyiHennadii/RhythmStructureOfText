package textsVocal.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import textsVocal.ru.DB_RussianDictionary;
import textsVocal.structure.ProsePortionForRythm;
import textsVocal.structure.VersePortionForRythm;

@Configuration
@ComponentScan(basePackages = "textsVocal")
@ConfigurationPropertiesScan({"coreText.textsVocal.config"})
public class AppConfigCore {

    @Bean
    DB_RussianDictionary db_russianDictionary() {
        return new DB_RussianDictionary();
    }

    @Bean
    @Scope("prototype")
    VersePortionForRythm versePortionForRythm() {
        return new VersePortionForRythm();
    }

    @Bean
    @Scope("prototype")
    ProsePortionForRythm prosePortionForRythm() {
        return new ProsePortionForRythm();
    }

}
