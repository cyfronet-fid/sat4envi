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

package pl.cyfronet.s4e.api;

import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import static pl.cyfronet.s4e.api.SearchApiParams.*;

@Service
@Slf4j
public class SearchConverter {
    public Map<String, Object> convertParams(String rowsSize, String rowStart, String orderby) {
        Map<String, Object> result = new HashMap<>();
        parseStringToInt(result, LIMIT, rowsSize);
        parseStringToInt(result, OFFSET, rowStart);
        parseOrderBy(result, orderby);
        return result;
    }

    public Map<String, Object> convert(String query) {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> queryParamMap = convertQueryToParamMap(query);

        setTime(queryParamMap, result, SENSING_START);
        setTime(queryParamMap, result, SENSING_END);
        setTime(queryParamMap, result, INGESTION_DATE);

        setTextParam(queryParamMap, result, PLATFORMNAME);
        setTextParam(queryParamMap, result, COLLECTION);
        setTextParam(queryParamMap, result, TIMELINESS);
        setTextParam(queryParamMap, result, PRODUCT_TYPE);
        setTextParam(queryParamMap, result, SENSOR_OPERATIONAL_MODE);
        setTextParam(queryParamMap, result, POLARISATION_MODE);
        setTextParam(queryParamMap, result, ORBIT_NUMBER);
        setTextParam(queryParamMap, result, LAST_ORBIT_NUMBER);
        setTextParam(queryParamMap, result, RELATIVE_ORBIT_NUMBER);
        setTextParam(queryParamMap, result, LAST_RELATIVE_ORBIT_NUMBER);

        setGeometryParam(queryParamMap, result, FOOTPRINT);

        setPercentageParam(queryParamMap, result, CLOUD_COVER_PERCENTAGE);
        return result;
    }

    private void parseStringToInt(Map<String, Object> result, String key, String value) {
        if (value != null) {
            result.put(key, Integer.parseInt(value));
        }
    }

    private void parseOrderBy(Map<String, Object> result, String orderBy) {
        if (orderBy != null) {
            String[] split = orderBy.split("\\s+");
            if (split.length == 2) {
                result.put(SORT_BY, parseSortBy(split[0]));
                result.put(ORDER, split[1]);
            }
        }
    }

    Map<String, String> convertQueryToParamMap(String query) {
        return Splitter.on("AND").omitEmptyStrings()
                .trimResults()
                .withKeyValueSeparator(
                        Splitter.on(':')
                                .limit(2)
                                .trimResults())
                .split(query);
    }

    private Object parseSortBy(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "beginposition":
            case "ingestiondate":
                return "";
            default:
                return null;
        }
    }

    private void setTime(Map<String, String> queryParamMap, Map<String, Object> result, String paramName) {
        if (queryParamMap.containsKey(paramName)) {
            String[] times = queryParamMap.get(paramName)
                    .replace("[", "")
                    .replace("]", "")
                    .replaceAll("\\s+", "")
                    .split("TO");
            String[] ingestionParams = getQueryParam(paramName).split(":");
            if (times.length != 2 || ingestionParams.length != 2)
                throw new DateTimeParseException(null, queryParamMap.get(paramName), 0);
            result.put(ingestionParams[0], parseTime(times[0]));
            result.put(ingestionParams[1], parseTime(times[1]));
        }
    }

    private String parseTime(String time) {
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        if (time.contains("NOW-")) {
            // NOW-NDAY(S)
            ZonedDateTime localTime = ZonedDateTime.now(ZoneId.of("UTC"));
            String[] times = time.split("-");
            switch (times[1].replaceAll("[0-9]", "")) {
                case "MINUTE":
                case "MINUTES":
                    return localTime.minusMinutes(Long.parseLong(times[1].replaceAll("[^0-9]", "")))
                            .format(dateTimeFormat);
                case "HOUR":
                case "HOURS":
                    return localTime.minusHours(Long.parseLong(times[1].replaceAll("[^0-9]", "")))
                            .format(dateTimeFormat);
                case "DAY":
                case "DAYS":
                    return localTime.minusDays(Long.parseLong(times[1].replaceAll("[^0-9]", "")))
                            .format(dateTimeFormat);
                case "MONTH":
                case "MONTHS":
                    return localTime.minusMonths(Long.parseLong(times[1].replaceAll("[^0-9]", "")))
                            .format(dateTimeFormat);
            }
            return localTime.format(dateTimeFormat);
        }
        if (time.equals("NOW")) {
            ZonedDateTime localTime = ZonedDateTime.now(ZoneId.of("UTC"));
            return localTime.format(dateTimeFormat);
        }
        return time;
    }

    private void setTextParam(Map<String, String> queryParamMap, Map<String, Object> result, String paramName) {
        if (queryParamMap.containsKey(paramName)) {
            String value = queryParamMap.get(paramName);
            result.put(getQueryParam(paramName), value);
        }
    }

    private void setGeometryParam(Map<String, String> queryParamMap, Map<String, Object> result, String paramName) {
        String queryParamValue = queryParamMap.get(paramName);
        if (queryParamValue == null) {
            return;
        }

        if (!queryParamValue.startsWith("Intersects(") || !queryParamValue.endsWith(")")) {
            return;
        }

        String geometry = queryParamValue.substring("Intersects(".length(), queryParamValue.length() - 1);

        if (geometry.startsWith("POLYGON((")) {
            result.put(getQueryParam(paramName), geometry);
        } else {
            result.put(getQueryParam(paramName), "POINT(" + geometry + ")");
        }
    }

    private void setPercentageParam(Map<String, String> queryParamMap, Map<String, Object> result, String paramName) {
        if (queryParamMap.containsKey(paramName)) {
            result.put(getQueryParam(paramName), Float.parseFloat(queryParamMap.get(paramName)));
        }
    }
}
