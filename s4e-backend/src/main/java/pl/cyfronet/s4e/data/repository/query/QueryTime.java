package pl.cyfronet.s4e.data.repository.query;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

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
        if (params.containsKey("sensingFrom")) {
            resultQuery.append(" AND " + getMetadataTime("sensing_time") + " >= ? ");
            parameters.add(parseDateToServerLocalDate(params.get("sensingFrom")));
        }
        if (params.containsKey("sensingTo")) {
            resultQuery.append(" AND " + getMetadataTime("sensing_time") + " <= ? ");
            parameters.add(parseDateToServerLocalDate(params.get("sensingTo")));
        }
    }

    private void queryByIngestionTime(Map<String, Object> params,
                                      List<Object> parameters,
                                      StringBuilder resultQuery) {
        // [TIMESTAMP] ingestionFrom / ingestionTo -> ingestion_time
        if (params.containsKey("ingestionFrom")) {
            resultQuery.append(" AND " + getMetadataTime("ingestion_time") + " >= ? ");
            parameters.add(parseDateToServerLocalDate(params.get("ingestionFrom")));
        }
        if (params.containsKey("ingestionTo")) {
            resultQuery.append(" AND " + getMetadataTime("ingestion_time") + " <= ? ");
            parameters.add(parseDateToServerLocalDate(params.get("ingestionTo")));
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
