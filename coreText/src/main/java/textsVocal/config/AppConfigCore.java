package textsVocal.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import textsVocal.ru.DB_RussianDictionary;
import textsVocal.structure.ProsePortionForRhythm;
import textsVocal.structure.VersePortionForRhythm;

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
    VersePortionForRhythm versePortionForRythm() {
        return new VersePortionForRhythm();
    }

    @Bean
    @Scope("prototype")
    ProsePortionForRhythm prosePortionForRythm() {
        return new ProsePortionForRhythm();
    }

}
