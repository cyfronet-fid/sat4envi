/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
