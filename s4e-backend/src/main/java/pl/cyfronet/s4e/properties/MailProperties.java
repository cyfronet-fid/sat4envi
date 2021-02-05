/*
 * Copyright 2021 ACC Cyfronet AGH
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

package pl.cyfronet.s4e.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@ConfigurationProperties("mail")
@Validated
@Setter
@Getter
public class MailProperties {
    /**
     * The base url for use in mails, for example to fetch images.
     * <p>
     * Without the trailing slash, including the protocol.
      */
    @NotBlank
    @Pattern(regexp = "https?://[^/]+")
    private String urlDomain;

    @NestedConfigurationProperty
    private LoggingMailSender loggingMailSender = new LoggingMailSender();

    @Getter
    @Setter
    public static class LoggingMailSender {
        /**
         * Whether to register LoggingMailSender bean.
         */
        private Boolean enabled = false;
    }
}
