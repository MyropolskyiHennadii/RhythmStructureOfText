package textsVocal.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "texts.Vocal")
public class AppConfig {

}

//
//@Configuration
//@ComponentScan(basePackages = "academy.learnprogramming")
//public class AppConfig {
//
//    // == bean methods ==
//    @Bean
//    public NumberGenerator numberGenerator() {
//        return new NumberGeneratorImpl();
//    }
//
//    @Bean
//    public Game game() {
//        return new GameImpl();
//    }
//
//    @Bean
//    public MessageGenerator messageGenerator() {
//        return new MessageGeneratorImpl();
//    }
//}