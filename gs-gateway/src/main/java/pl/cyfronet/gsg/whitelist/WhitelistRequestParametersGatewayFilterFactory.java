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

package pl.cyfronet.gsg.whitelist;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

import java.util.HashSet;
import java.util.Set;

public class WhitelistRequestParametersGatewayFilterFactory extends AbstractGatewayFilterFactory<WhitelistRequestParametersGatewayFilterFactory.Config> {
    public WhitelistRequestParametersGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        val allowedParams = config.getAllowedParams();
        return new WhitelistRequestParametersGatewayFilter(allowedParams);
    }

    @Setter
    @Getter
    public static class Config {
        private Set<String> allowedParams = new HashSet<>();
    }
}
