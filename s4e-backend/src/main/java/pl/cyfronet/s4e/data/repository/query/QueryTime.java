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

package pl.cyfronet.s4e.data.repository.query;

import org.springframework.validation.Errors;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import static pl.cyfronet.s4e.search.SearchQueryParams.*;

public class QueryTime extends QueryDecorator {

    public QueryTime(QueryBuilder queryBuilder) {
        super(queryBuilder);
    }

    @Override
    public void doPrepareQueryAndParameters(Map<String, Object> params,
                                            List<Object> parameters,
                                            StringBuilder resultQuery,
                                            Errors errors) {
        queryBySensingTime(params, parameters, resultQuery, errors);
        queryByIngestionTime(params, parameters, resultQuery, errors);
    }

    private void queryBySensingTime(Map<String, Object> params,
                                    List<Object> parameters,
                                    StringBuilder resultQuery,
                                    Errors errors) {
        // [TIMESTAMP] sensingFrom / sensingTo -> sensing_time
        if (params.containsKey(SENSING_FROM)) {
            resultQuery.append(" AND ").append(getMetadataTime("sensing_time")).append(" >= ?");
            parameters.add(parseDateToServerLocalDate(params, SENSING_FROM, errors));
        }
        if (params.containsKey(SENSING_TO)) {
            resultQuery.append(" AND ").append(getMetadataTime("sensing_time")).append(" <= ?");
            parameters.add(parseDateToServerLocalDate(params, SENSING_TO, errors));
        }
    }

    private void queryByIngestionTime(Map<String, Object> params,
                                      List<Object> parameters,
                                      StringBuilder resultQuery,
                                      Errors errors) {
        // [TIMESTAMP] ingestionFrom / ingestionTo -> ingestion_time
        if (params.containsKey(INGESTION_FROM)) {
            resultQuery.append(" AND ").append(getMetadataTime("ingestion_time")).append(" >= ?");
            parameters.add(parseDateToServerLocalDate(params, INGESTION_FROM, errors));
        }
        if (params.containsKey(INGESTION_TO)) {
            resultQuery.append(" AND ").append(getMetadataTime("ingestion_time")).append(" <= ?");
            parameters.add(parseDateToServerLocalDate(params, INGESTION_TO, errors));
        }
    }

    private String getMetadataTime(String timeParameter) {
        return "f_cast_isots(metadata_content->>'" + timeParameter + "')";
    }

    private LocalDateTime parseDateToServerLocalDate(Map<String, Object> params, String param, Errors errors) {
        // change date to server zone
        try {
            DateTimeFormatter dateTimeFormat = DateTimeFormatter.ISO_DATE_TIME;
            ZonedDateTime clientTime = ZonedDateTime.parse(String.valueOf(params.get(param)), dateTimeFormat);
            return clientTime.withZoneSameInstant(ZoneId.of("UTC"))
                    .toLocalDateTime();
        } catch (DateTimeParseException e) {
            errors.rejectValue(param, "pl.cyfronet.s4e.data.repository.query.QueryTime.message", new Object[]{params.get(param)}, e.getMessage());
            return LocalDateTime.now();
        }
    }
}
