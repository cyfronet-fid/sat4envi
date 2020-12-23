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

package pl.cyfronet.s4e.data.repository.query;

import org.springframework.validation.Errors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static pl.cyfronet.s4e.search.SearchQueryParams.*;

public class QueryText extends QueryDecorator {
    Map<String, String> paramsToDBField;

    public QueryText(QueryBuilder queryBuilder) {
        super(queryBuilder);
        paramsToDBField = new LinkedHashMap<>();
        paramsToDBField.put(SATELLITE_PLATFORM, "spacecraft");
        paramsToDBField.put(PRODUCT_TYPE, "product_type");
        paramsToDBField.put(POLARISATION, "polarisation");
        paramsToDBField.put(PROCESSING_LEVEL, "processing_level");
        paramsToDBField.put(SENSOR_MODE, "sensor_mode");
        paramsToDBField.put(COLLECTION, "collection");
        paramsToDBField.put(TIMELINESS, "timeliness");
        paramsToDBField.put(INSTRUMENT, "instrument");
        paramsToDBField.put(PRODUCT_LEVEL, "product_level");
        paramsToDBField.put(RELATIVE_ORBIT_NUMBER, "relative_orbit_number");
        paramsToDBField.put(ABSOLUTE_ORBIT_NUMBER, "absolute_orbit_number");
    }

    @Override
    public void doPrepareQueryAndParameters(Map<String, Object> params,
                                            List<Object> parameters,
                                            StringBuilder resultQuery,
                                            Errors errors) {
        paramsToDBField.entrySet().forEach(entry -> queryBy(entry, params, parameters, resultQuery));
    }

    private void queryBy(Map.Entry<String, String> entry,
                         Map<String, Object> params,
                         List<Object> parameters,
                         StringBuilder resultQuery) {
        if (params.containsKey(entry.getKey())) {
            resultQuery.append(" AND metadata_content->>'" + entry.getValue() + "' = ? ");
            if (params.containsKey(POLARISATION)) {
                parameters.add(parsePolarisation(String.valueOf(params.get(entry.getKey()))));
            } else {
                parameters.add(String.valueOf(params.get(entry.getKey())));
            }
        }
    }

    private String parsePolarisation(String s) {
        String[] split = s.split("[+|\\s]+");
        if (split.length > 1) {
            return "Dual " + split[0] + "/" + split[1];
        }
        return split[0];
    }
}
