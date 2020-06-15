package pl.cyfronet.s4e.data.repository.query;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
                                            StringBuilder resultQuery) {
        queryBySensingTime(params, parameters, resultQuery);
        queryByIngestionTime(params, parameters, resultQuery);
    }

    private void queryBySensingTime(Map<String, Object> params,
                                    List<Object> parameters,
                                    StringBuilder resultQuery) {
        // [TIMESTAMP] sensingFrom / sensingTo -> sensing_time
        if (params.containsKey(SENSING_FROM)) {
            resultQuery.append(" AND " + getMetadataTime("sensing_time") + " >= ? ");
            parameters.add(parseDateToServerLocalDate(params.get(SENSING_FROM)));
        }
        if (params.containsKey(SENSING_TO)) {
            resultQuery.append(" AND " + getMetadataTime("sensing_time") + " <= ? ");
            parameters.add(parseDateToServerLocalDate(params.get(SENSING_TO)));
        }
    }

    private void queryByIngestionTime(Map<String, Object> params,
                                      List<Object> parameters,
                                      StringBuilder resultQuery) {
        // [TIMESTAMP] ingestionFrom / ingestionTo -> ingestion_time
        if (params.containsKey(INGESTION_FROM)) {
            resultQuery.append(" AND " + getMetadataTime("ingestion_time") + " >= ? ");
            parameters.add(parseDateToServerLocalDate(params.get(INGESTION_FROM)));
        }
        if (params.containsKey(INGESTION_TO)) {
            resultQuery.append(" AND " + getMetadataTime("ingestion_time") + " <= ? ");
            parameters.add(parseDateToServerLocalDate(params.get(INGESTION_TO)));
        }
    }

    private String getMetadataTime(String timeParameter) {
        return "to_timestamp(metadata_content->>'" + timeParameter + "', '" + DATE_FORMAT + "')";
    }

    private LocalDateTime parseDateToServerLocalDate(Object date) {
        // change date to server zone
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ISO_DATE_TIME;
        ZonedDateTime clientTime = ZonedDateTime.parse(String.valueOf(date), dateTimeFormat);
        return clientTime.withZoneSameInstant(ZoneId.of("UTC"))
                .toLocalDateTime();
    }
}
