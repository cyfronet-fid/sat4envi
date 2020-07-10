package pl.cyfronet.s4e.data.repository.query;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@BasicTest
@Slf4j
public class QueryBuilderTest {
    @Autowired
    private QueryBuilder queryBuilder;

    @Test
    public void shouldReturnWholeQuery() {
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
        query.append("SELECT id,product_id,ST_AsText(ST_Transform(footprint,4326)) AS footprint,");
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
        queryBuilder.prepareQueryAndParameters(params, parameters, resultQuery);
        assertThat(resultQuery.toString(), is(equalTo(query.toString())));
    }

    @Test
    public void shouldReturnQueryWithoutAnyConditions() {
        List<Object> parameters = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        StringBuilder resultQuery = new StringBuilder();
        StringBuilder query = new StringBuilder();
        query.append("SELECT id,product_id,");
        query.append("ST_AsText(ST_Transform(footprint,4326)) AS footprint,");
        query.append("metadata_content,scene_content,timestamp ");
        query.append("FROM Scene ");
        query.append("WHERE true  ORDER BY id DESC LIMIT ?  OFFSET ? ;");
        queryBuilder.prepareQueryAndParameters(params, parameters, resultQuery);
        assertThat(resultQuery.toString(), is(equalTo(query.toString())));
    }
}
