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

package pl.cyfronet.s4e.search;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.cyfronet.s4e.search.SearchPanelConfigResponse.*;

import java.util.List;

import static pl.cyfronet.s4e.search.SearchQueryParams.*;

@Configuration
public class SearchPanelConfig {
    @Bean
    public List<SectionPrototype> searchPanelSectionPrototypes() {
        return List.of(
                new SectionPrototype(
                        "Sentinel-1",
                        "Sentinel-1.metadata.v1.json",
                        List.of(
                                new DatetimeParamPrototype(INGESTION_FROM),
                                new DatetimeParamPrototype(INGESTION_TO)
                        ),
                        List.of(
                                new SelectParamPrototype(SATELLITE_PLATFORM, List.of("", "Sentinel-1A", "Sentinel-1B")),
                                new SelectParamPrototype(PROCESSING_LEVEL, List.of("", "1", "2")),
                                new SelectParamPrototype(POLARISATION, List.of("", "HH", "VV", "HV", "VH", "HH+HV", "VV+VH")),
                                new SelectParamPrototype(SENSOR_MODE, List.of("", "SM", "IW", "EW", "WV")),
                                new TextParamPrototype(RELATIVE_ORBIT_NUMBER)
                        )
                ),
                new SectionPrototype(
                        "Sentinel-2",
                        "Sentinel-2.metadata.v1.json",
                        List.of(
                                new DatetimeParamPrototype(INGESTION_FROM),
                                new DatetimeParamPrototype(INGESTION_TO)
                        ),
                        List.of(
                                new SelectParamPrototype(SATELLITE_PLATFORM, List.of("", "Sentinel-2A", "Sentinel-2B")),
                                new SelectParamPrototype(PROCESSING_LEVEL, List.of("", "Level-1C", "Level-2A")),
                                new TextParamPrototype(RELATIVE_ORBIT_NUMBER),
                                new FloatParamPrototype(CLOUD_COVER, 0., 100.)
                        )
                ),
                new SectionPrototype(
                        "MSG",
                        "MSG.metadata.v1.json",
                        List.of(),
                        List.of()
                )
        );
    }
}
