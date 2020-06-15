package pl.cyfronet.s4e.data.repository.query;

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
        paramsToDBField.put("collection", "collection");
        paramsToDBField.put("timeliness", "timeliness");
        paramsToDBField.put("instrument", "instrument");
        paramsToDBField.put("productLevel", "product_level");
        paramsToDBField.put(RELATIVE_ORBIT_NUMBER, "relative_orbit_number");
        paramsToDBField.put("absoluteOrbitNumber", "absolute_orbit_number");
    }

    @Override
    public void doPrepareQueryAndParameters(Map<String, Object> params,
                                                   List<Object> parameters,
                                                   StringBuilder resultQuery) {
        paramsToDBField.entrySet().forEach(entry -> queryBy(entry, params, parameters, resultQuery));
    }

    private void queryBy(Map.Entry<String, String> entry,
                         Map<String, Object> params,
                         List<Object> parameters,
                         StringBuilder resultQuery) {
        if (params.containsKey(entry.getKey())) {
            resultQuery.append(" AND metadata_content->>'" + entry.getValue() + "' = ? ");
            parameters.add(String.valueOf(params.get(entry.getKey())));
        }
    }
}
