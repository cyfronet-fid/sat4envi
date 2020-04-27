package pl.cyfronet.s4e.data.repository.query;

import java.util.List;
import java.util.Map;

public class QueryTime extends QueryDecorator {
    public static final String dateFormat = "YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"";

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
            parameters.add(params.get("sensingFrom"));
        }
        if (params.containsKey("sensingTo")) {
            resultQuery.append(" AND " + getMetadataTime("sensing_time") + " <= ? ");
            parameters.add(params.get("sensingTo"));
        }
    }

    private void queryByIngestionTime(Map<String, Object> params,
                                      List<Object> parameters,
                                      StringBuilder resultQuery) {
        // [TIMESTAMP] ingestionFrom / ingestionTo -> ingestion_time
        if (params.containsKey("ingestionFrom")) {
            resultQuery.append(" AND " + getMetadataTime("ingestion_time") + " >= ? ");
            parameters.add(params.get("ingestionFrom"));
        }
        if (params.containsKey("ingestionTo")) {
            resultQuery.append(" AND " + getMetadataTime("ingestion_time") + " <= ? ");
            parameters.add(params.get("ingestionTo"));
        }
    }

    private String getMetadataTime(String timeParameter) {
        return "to_timestamp(metadata_content->>'" + timeParameter + "', '" + dateFormat + "')";
    }
}
