package pl.cyfronet.s4e.config;

import lombok.val;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class I18nConfig {
    @Bean
    public MessageSource messageSource() {
        val resourceBundleMessageSource = new ResourceBundleMessageSource();
        resourceBundleMessageSource.setDefaultEncoding("UTF-8");
        resourceBundleMessageSource.setBasename("messages");
        // Always use special escaping of characters such as ' => "'".
        // Consult https://docs.oracle.com/javase/8/docs/api/java/text/MessageFormat.html
        // in case of problems.
        // resourceBundleMessageSource.setAlwaysUseMessageFormat(true);
        //
        // See: https://github.com/spring-projects/spring-framework/issues/11396#issuecomment-453347114
        // If this is set to false, then interpolation of statements such as '{min}' in validation messages work.
        // Doesn't work with true...
        resourceBundleMessageSource.setAlwaysUseMessageFormat(false);
        return resourceBundleMessageSource;
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource());
        return bean;
    }

}
