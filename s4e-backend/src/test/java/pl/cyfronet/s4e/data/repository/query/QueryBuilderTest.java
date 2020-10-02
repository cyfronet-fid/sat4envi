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
        StringBuilder query = new StringBuilder();
        query.append("SELECT id,product_id,scene_key,ST_AsText(ST_Transform(footprint,4326)) AS footprint,");
        query.append("metadata_content,scene_content,timestamp ");
        query.append("FROM Scene WHERE true  ");
        query.append("AND to_timestamp(metadata_content->>'sensing_time', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') >= ?  ");
        query.append("AND to_timestamp(metadata_content->>'sensing_time', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') <= ?  ");
        query.append("AND to_timestamp(metadata_content->>'ingestion_time', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') >= ?  ");
        query.append("AND to_timestamp(metadata_content->>'ingestion_time', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') <= ?  ");
        query.append("AND metadata_content->>'spacecraft' = ?  AND metadata_content->>'product_type' = ?  ");
        query.append("AND metadata_content->>'polarisation' = ?  AND metadata_content->>'processing_level' = ?  ");
        query.append("AND metadata_content->>'relative_orbit_number' = ?  ");
        query.append("AND (metadata_content ->> 'cloud_cover')::float <= ?  ORDER BY id DESC LIMIT ?  OFFSET ? ;");
        queryBuilder.prepareQueryAndParameters(params, parameters, resultQuery, errors);
        assertThat(resultQuery.toString(), is(equalTo(query.toString())));
    }

    @Test
    public void shouldReturnQueryWithoutAnyConditions() {
        Errors errors = new MapBindingResult(new HashMap<>(), "params");
        List<Object> parameters = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        StringBuilder resultQuery = new StringBuilder();
        StringBuilder query = new StringBuilder();
        query.append("SELECT id,product_id,scene_key,");
        query.append("ST_AsText(ST_Transform(footprint,4326)) AS footprint,");
        query.append("metadata_content,scene_content,timestamp ");
        query.append("FROM Scene ");
        query.append("WHERE true  ORDER BY id DESC LIMIT ?  OFFSET ? ;");
        queryBuilder.prepareQueryAndParameters(params, parameters, resultQuery, errors);
        assertThat(resultQuery.toString(), is(equalTo(query.toString())));
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
