package pl.cyfronet.s4e;

import lombok.val;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

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
        resourceBundleMessageSource.setAlwaysUseMessageFormat(true);
        return resourceBundleMessageSource;
    }
}
