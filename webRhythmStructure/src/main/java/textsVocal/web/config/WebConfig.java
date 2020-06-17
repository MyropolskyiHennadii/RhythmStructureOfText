package textsVocal.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import textsVocal.config.AppConfigCore;
import textsVocal.config.CommonConstants;

@Configuration
@Import(AppConfigCore.class)
public class WebConfig implements WebMvcConfigurer {

    @Bean //change locale by session
    public LocaleResolver localeResolver(){
        return new SessionLocaleResolver();
    }

    @Bean("commonConstants")
    public CommonConstants commonConstants() {
        return new CommonConstants();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("locale");
        registry.addInterceptor(localeChangeInterceptor);
    }
}
