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

package pl.cyfronet.s4e.db.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.bean.Property;
import pl.cyfronet.s4e.data.repository.PropertyRepository;

@Profile({"development", "run-seed-analytics-config"})
@Component
@RequiredArgsConstructor
@Slf4j
public class SeedAnalyticsConfig implements ApplicationRunner {
    private static final String DEVELOPMENT_ANALYTICS_CONFIG = "type=disabled";

    private final PropertyRepository propertyRepository;

    @Async
    @Override
    public void run(ApplicationArguments args) {
        if (propertyRepository.findByName(Constants.PROPERTY_ANALYTICS_CONFIG).isPresent()) {
            log.info("Analytics config already set, skipping");
            return;
        }

        propertyRepository.save(Property.builder()
                .name(Constants.PROPERTY_ANALYTICS_CONFIG)
                .value(DEVELOPMENT_ANALYTICS_CONFIG)
                .build());

        log.info("Analytics config set to " + DEVELOPMENT_ANALYTICS_CONFIG);
    }
}
