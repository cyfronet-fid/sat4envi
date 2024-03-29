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

package pl.cyfronet.s4e;

import io.sentry.Sentry;
import org.springdoc.core.SpringDocUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@ConfigurationPropertiesScan("pl.cyfronet.s4e.properties")
@EnableCaching
public class Application {

    public static void main(String[] args) {
        Sentry.init(options -> {
            options.setDsn("");
            options.setTracesSampleRate(0.5);
            options.setEnableExternalConfiguration(true);
        });

        SpringDocUtils.getConfig().replaceWithClass(
                org.springframework.data.domain.Pageable.class,
                org.springdoc.core.converters.models.Pageable.class
        );
        SpringApplication.run(Application.class, args);
    }

}
