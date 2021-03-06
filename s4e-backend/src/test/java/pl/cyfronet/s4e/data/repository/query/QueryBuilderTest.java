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

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import pl.cyfronet.s4e.BasicTest;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static pl.cyfronet.s4e.search.SearchQueryParams.*;

@BasicTest
@Slf4j
public class QueryBuilderTest {
    @Autowired
    private QueryBuilder queryBuilder;

    @Test
    public void shouldReturnWholeQuery() {
        Errors errors = new MapBindingResult(new HashMap<>(), "params");
        List<Object> parameters = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("relativeOrbitNumber", 2);
        params.put("processingLevel", "2LC");
        params.put("cloudCover", 0.5f);
        params.put("polarisation", "Dual VV/VH");
        params.put("productType", "GRDH");
        params.put("satellitePlatform", "Sentinel-1A");
        params.put("sensingFrom", "2019-11-09T00:00:00.000000+00:00");
        params.put("sensingTo", "2019-11-12T00:00:00.000000+00:00");
        params.put("ingestionFrom", "2019-11-09T00:00:00.000000+00:00");
        params.put("ingestionTo", "2019-11-12T00:00:00.000000+00:00");
        StringBuilder resultQuery = new StringBuilder();
        queryBuilder.prepareQueryAndParameters(params, parameters, resultQuery, errors);
        String query = "SELECT " +
                "scene.id, product_id, scene_key, ST_AsText(ST_Transform(footprint,4326),5) AS footprint, " +
                "metadata_content, scene_content, timestamp " +
                "FROM scene JOIN product ON scene.product_id = product.id " +
                "WHERE true " +
                "AND product.name = ? " +
                "AND f_cast_isots(metadata_content->>'sensing_time') >= ? " +
                "AND f_cast_isots(metadata_content->>'sensing_time') <= ? " +
                "AND f_cast_isots(metadata_content->>'ingestion_time') >= ? " +
                "AND f_cast_isots(metadata_content->>'ingestion_time') <= ? " +
                "AND metadata_content->>'spacecraft' = ? " +
                "AND metadata_content->>'polarisation' = ? " +
                "AND metadata_content->>'processing_level' = ? " +
                "AND metadata_content->>'relative_orbit_number' = ? " +
                "AND (metadata_content ->> 'cloud_cover')::float <= ? " +
                "ORDER BY id DESC LIMIT ? OFFSET ?;";
        assertThat(resultQuery.toString(), is(equalTo(query)));
    }

    @Test
    public void shouldReturnQueryWithoutAnyConditions() {
        Errors errors = new MapBindingResult(new HashMap<>(), "params");
        List<Object> parameters = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        StringBuilder resultQuery = new StringBuilder();
        queryBuilder.prepareQueryAndParameters(params, parameters, resultQuery, errors);
        String query = "SELECT " +
                "scene.id, product_id, scene_key, ST_AsText(ST_Transform(footprint,4326),5) AS footprint, " +
                "metadata_content, scene_content, timestamp " +
                "FROM scene JOIN product ON scene.product_id = product.id " +
                "WHERE true ORDER BY id DESC LIMIT ? OFFSET ?;";
        assertThat(resultQuery.toString(), is(equalTo(query)));
    }

    @Test
    public void shouldReturnErrorsForTime() {
        Errors errors = new MapBindingResult(new HashMap<>(), "params");
        List<Object> parameters = new ArrayList<>();
        StringBuilder resultQuery = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        params.put(INGESTION_FROM, "2019-11-08");
        queryBuilder.prepareQueryAndParameters(params, parameters, resultQuery, errors);
        assertThat(errors.hasErrors(), is(true));
        assertThat(errors.getFieldError(INGESTION_FROM).getDefaultMessage(),
                is(equalTo("Text '2019-11-08' could not be parsed at index 10")));

        errors = new MapBindingResult(new HashMap<>(), "params");
        parameters = new ArrayList<>();
        resultQuery = new StringBuilder();
        params = new HashMap<>();
        params.put(INGESTION_FROM, "Hakuna matata");
        queryBuilder.prepareQueryAndParameters(params, parameters, resultQuery, errors);
        assertThat(errors.hasErrors(), is(true));
        assertThat(errors.getFieldError(INGESTION_FROM).getDefaultMessage(),
                is(equalTo("Text 'Hakuna matata' could not be parsed at index 0")));
    }

    @ParameterizedTest
    @MethodSource
    public void shouldReturnErrorsForParam(String param, Object value, boolean correct) {
        Errors errors = new MapBindingResult(new HashMap<>(), "params");
        List<Object> parameters = new ArrayList<>();
        StringBuilder resultQuery = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        params.put(param, value);
        queryBuilder.prepareQueryAndParameters(params, parameters, resultQuery, errors);
        assertThat(errors.hasErrors(), is(correct));
    }

    private static Stream<Arguments> shouldReturnErrorsForParam() {
        return Stream.of(
                Arguments.of(LIMIT, 0, false),
                Arguments.of(LIMIT, 50, false),
                Arguments.of(LIMIT, Integer.MAX_VALUE, false),
                Arguments.of(LIMIT, "abc", true),
                Arguments.of(LIMIT, -1, true),
                Arguments.of(OFFSET, 0, false),
                Arguments.of(OFFSET, 10, false),
                Arguments.of(OFFSET, "abc", true),
                Arguments.of(OFFSET, -1, true),
                Arguments.of(CLOUD_COVER, 0, false),
                Arguments.of(CLOUD_COVER, 99.99993, false),
                Arguments.of(CLOUD_COVER, 100, false),
                Arguments.of(CLOUD_COVER, "abc", true),
                Arguments.of(CLOUD_COVER, -1, true),
                Arguments.of(CLOUD_COVER, 101, true),
                Arguments.of(CLOUD_COVER, Integer.MIN_VALUE, true),
                Arguments.of(CLOUD_COVER, Integer.MAX_VALUE, true),
                Arguments.of(INGESTION_FROM, "2020-06-16T23:59:59.999Z", false),
                Arguments.of(INGESTION_FROM, ZonedDateTime.now(), false),
                Arguments.of(INGESTION_FROM, "2019-11-08", true),
                Arguments.of(INGESTION_FROM, "Hakuna matata", true)
        );
    }
}
