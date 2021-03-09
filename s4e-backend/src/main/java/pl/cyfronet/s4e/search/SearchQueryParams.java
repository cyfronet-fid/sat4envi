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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

public class SearchQueryParams {
    public static final String SORT_BY = "sortBy";
    public static final String ORDER = "order";
    public static final String LIMIT = "limit";
    public static final String OFFSET = "offset";

    public static final String SENSING_FROM = "sensingFrom";
    public static final String SENSING_TO = "sensingTo";

    public static final String INGESTION_FROM = "ingestionFrom";
    public static final String INGESTION_TO = "ingestionTo";

    public static final String SATELLITE_PLATFORM = "satellitePlatform";
    public static final String PRODUCT_TYPE = "productType";
    public static final String PROCESSING_LEVEL = "processingLevel";
    public static final String POLARISATION = "polarisation";
    public static final String SENSOR_MODE = "sensorMode";
    public static final String RELATIVE_ORBIT_NUMBER = "relativeOrbitNumber";
    public static final String ABSOLUTE_ORBIT_NUMBER = "absoluteOrbitNumber";
    public static final String COLLECTION = "collection";
    public static final String TIMELINESS = "timeliness";
    public static final String INSTRUMENT = "instrument";
    public static final String FOOTPRINT = "footprint";
    public static final String PRODUCT_LEVEL = "productLevel";

    public static final String CLOUD_COVER = "cloudCover";


    public static final String ACCESS_TYPE = "accessType";

    @Target({METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(name = SENSING_FROM, in = ParameterIn.QUERY)
    @Parameter(name = SENSING_TO, in = ParameterIn.QUERY)
    @Parameter(name = INGESTION_FROM, in = ParameterIn.QUERY)
    @Parameter(name = INGESTION_TO, in = ParameterIn.QUERY)
    @Parameter(name = SATELLITE_PLATFORM, in = ParameterIn.QUERY)
    @Parameter(name = PRODUCT_TYPE, in = ParameterIn.QUERY, required = true)
    @Parameter(name = PROCESSING_LEVEL, in = ParameterIn.QUERY)
    @Parameter(name = POLARISATION, in = ParameterIn.QUERY)
    @Parameter(name = SENSOR_MODE, in = ParameterIn.QUERY)
    @Parameter(name = RELATIVE_ORBIT_NUMBER, in = ParameterIn.QUERY)
    @Parameter(name = ABSOLUTE_ORBIT_NUMBER, in = ParameterIn.QUERY)
    @Parameter(name = COLLECTION, in = ParameterIn.QUERY)
    @Parameter(name = TIMELINESS, in = ParameterIn.QUERY)
    @Parameter(name = INSTRUMENT, in = ParameterIn.QUERY)
    @Parameter(name = FOOTPRINT, in = ParameterIn.QUERY)
    @Parameter(name = PRODUCT_LEVEL, in = ParameterIn.QUERY)
    @Parameter(name = CLOUD_COVER, in = ParameterIn.QUERY)
    public @interface QueryParameters {}

    @Target({METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Parameter(name = SORT_BY, in = ParameterIn.QUERY)
    @Parameter(name = ORDER, in = ParameterIn.QUERY)
    @Parameter(name = LIMIT, in = ParameterIn.QUERY)
    @Parameter(name = OFFSET, in = ParameterIn.QUERY)
    public @interface PagingParameters {}
}
