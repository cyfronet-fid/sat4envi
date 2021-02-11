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

package pl.cyfronet.s4e.helpdesk;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.bean.Property;
import pl.cyfronet.s4e.data.repository.PropertyRepository;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class HelpdeskConfigSupplier {
    private final PropertyRepository propertyRepository;

    public Map<String, String> getConfig() {
        return propertyRepository.findByName(Constants.PROPERTY_HELPDESK_CONFIG)
            .map(Property::getValue)
            .filter(not(String::isBlank))
            .stream()
                .flatMap(value -> Arrays.stream(value.split(",")))
                .map(entry -> entry.split("=", 2))
                .filter(split -> split.length == 2)
                .collect(Collectors.toUnmodifiableMap(
                        split -> split[0],
                        split -> split[1],
                        (v1, v2) -> v1
                ));
    }
}
