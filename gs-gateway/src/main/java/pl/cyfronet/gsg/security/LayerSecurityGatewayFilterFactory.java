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

package pl.cyfronet.gsg.security;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LayerSecurityGatewayFilterFactory extends AbstractGatewayFilterFactory<LayerSecurityGatewayFilterFactory.Config> {
    private final Clock clock;

    public LayerSecurityGatewayFilterFactory(Clock clock) {
        super(Config.class);
        this.clock = clock;
    }

    @Override
    public GatewayFilter apply(Config config) {
        val layersQueryParam = config.getLayersQueryParam();
        val timeQueryParam = config.getTimeQueryParam();
        val freshDuration = config.getFreshDuration();
        val access = config.getAccess();

        return new LayerSecurityGatewayFilter(clock, layersQueryParam, timeQueryParam, freshDuration, access);
    }

    @Setter
    @Getter
    public static class Config {
        private String layersQueryParam = "LAYERS";
        private String timeQueryParam = "TIME";

        private Duration freshDuration = Duration.ofHours(3);

        private Map<String, AccessType> access = new HashMap<>();
    }
}
