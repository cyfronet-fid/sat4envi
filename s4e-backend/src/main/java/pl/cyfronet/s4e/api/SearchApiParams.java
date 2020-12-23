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

package pl.cyfronet.s4e.api;

import pl.cyfronet.s4e.search.SearchQueryParams;

public class SearchApiParams {
    public static final String SORT_BY = "sortBy";
    public static final String ORDER = "order";
    public static final String LIMIT = "limit";
    public static final String OFFSET = "offset";

    public static final String SENSING_START = "beginposition";
    public static final String SENSING_END = "endposition";
    public static final String INGESTION_DATE = "ingestiondate";

    public static final String PLATFORMNAME = "platformname";
    public static final String COLLECTION = "collection";
    public static final String TIMELINESS = "timeliness";
    public static final String PRODUCT_TYPE = "producttype";
    public static final String PROCESSING_LEVEL = "processinglevel";
    public static final String POLARISATION_MODE = "polarisationmode";
    public static final String SENSOR_OPERATIONAL_MODE = "sensoroperationalmode";
    public static final String ORBIT_NUMBER = "orbitnumber";
    public static final String LAST_ORBIT_NUMBER = "lastorbitnumber";
    public static final String RELATIVE_ORBIT_NUMBER = "relativeorbitnumber";
    public static final String LAST_RELATIVE_ORBIT_NUMBER = "lastrelativeorbitnumber";
    public static final String INSTRUMENT = "instrument";
    public static final String PRODUCT_LEVEL = "productlevel";
    public static final String FOOTPRINT = "footprint";

    public static final String CLOUD_COVER_PERCENTAGE = "cloudcoveragepercentage";

    public static String getQueryParam(String param) {
        switch (param) {
            case SENSING_START:
            case SENSING_END:
                return SearchQueryParams.SENSING_FROM + ":" + SearchQueryParams.SENSING_TO;
            case INGESTION_DATE:
                return SearchQueryParams.INGESTION_FROM + ":" + SearchQueryParams.INGESTION_TO;
            case PLATFORMNAME:
                return SearchQueryParams.SATELLITE_PLATFORM;
            case COLLECTION:
                return SearchQueryParams.COLLECTION;
            case TIMELINESS:
                return SearchQueryParams.TIMELINESS;
            case FOOTPRINT:
                return SearchQueryParams.FOOTPRINT;
            case PRODUCT_TYPE:
                return SearchQueryParams.PRODUCT_TYPE;
            case SENSOR_OPERATIONAL_MODE:
                return SearchQueryParams.SENSOR_MODE;
            case ORBIT_NUMBER:
            case LAST_ORBIT_NUMBER:
                return SearchQueryParams.ABSOLUTE_ORBIT_NUMBER;
            case RELATIVE_ORBIT_NUMBER:
            case LAST_RELATIVE_ORBIT_NUMBER:
                return SearchQueryParams.RELATIVE_ORBIT_NUMBER;
            case POLARISATION_MODE:
                return SearchQueryParams.POLARISATION;
            case CLOUD_COVER_PERCENTAGE:
                return SearchQueryParams.CLOUD_COVER;
            default:
                return null;
        }
    }
}
