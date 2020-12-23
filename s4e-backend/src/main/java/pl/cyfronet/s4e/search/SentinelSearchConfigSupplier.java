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

package pl.cyfronet.s4e.search;

import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.search.SentinelSearchConfig.*;

import java.util.List;
import java.util.function.Supplier;

import static pl.cyfronet.s4e.search.SearchQueryParams.*;

@Component
public class SentinelSearchConfigSupplier implements Supplier<SentinelSearchConfig> {
    private final SentinelSearchConfig config = new SentinelSearchConfig(
            new Common(List.of(
                    new SelectParam(SORT_BY, List.of("sensingTime", "ingestionTime", "id")),
                    new SelectParam(ORDER, List.of("DESC", "ASC")),
                    new DatetimeParam(SENSING_FROM),
                    new DatetimeParam(SENSING_TO),
                    new DatetimeParam(INGESTION_FROM),
                    new DatetimeParam(INGESTION_TO)
            )),
            List.of(
                    new Section("sentinel-1", List.of(
                            new SelectParam(SATELLITE_PLATFORM, List.of("Sentinel-1A", "Sentinel-1B")),
                            new SelectParam(PRODUCT_TYPE, List.of("", "GRDM", "GRDH", "SLC_")),
                            new SelectParam(PROCESSING_LEVEL, List.of("", "1", "2")),
                            new SelectParam(POLARISATION, List.of("", "HH", "VV", "HV", "VH", "HH+HV", "VV+VH")),
                            new SelectParam(SENSOR_MODE, List.of("", "SM", "IW", "EW", "WV")),
                            new TextParam(RELATIVE_ORBIT_NUMBER)
                    )),
                    new Section("sentinel-2", List.of(
                            new SelectParam(SATELLITE_PLATFORM, List.of("Sentinel-2A", "Sentinel-2B")),
                            new SelectParam(PROCESSING_LEVEL, List.of("", "Level-1C", "Level-2A")),
                            new TextParam(RELATIVE_ORBIT_NUMBER),
                            new FloatParam(CLOUD_COVER, 0., 100.)
                    ))
            )
    );

    @Override
    public SentinelSearchConfig get() {
        return config;
    }
}
