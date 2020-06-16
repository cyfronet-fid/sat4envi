package pl.cyfronet.s4e.data.repository.query;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;

import java.util.List;
import java.util.Map;

import static pl.cyfronet.s4e.search.SearchQueryParams.ORDER;
import static pl.cyfronet.s4e.search.SearchQueryParams.SORT_BY;

public class QueryEnding extends QueryDecorator {
    private final SpringDataWebProperties properties;

    public QueryEnding(QueryBuilder queryBuilder, SpringDataWebProperties properties) {
        super(queryBuilder);
        this.properties = properties;
    }

    @Override
    public void doPrepareQueryAndParameters(Map<String, Object> params,
                                            List<Object> parameters,
                                            StringBuilder resultQuery) {
        resultQuery.append(" ORDER BY " + getOrderByField(params) + " " + getOrder(params));
        resultQuery.append(" LIMIT ? ");
        parameters.add(getLimit(params));
        resultQuery.append(" OFFSET ? ;");
        parameters.add(Integer.parseInt(params.getOrDefault("offset", 0).toString()));
    }

    private String getOrderByField(Map<String, Object> params) {
        switch (String.valueOf(params.getOrDefault(SORT_BY, "id"))) {
            case "ingestionTime":
                return "to_timestamp(metadata_content->>'ingestion_time', '" + DATE_FORMAT + "')";
            case "sensingTime":
                return "to_timestamp(metadata_content->>'sensing_time', '" + DATE_FORMAT + "')";
            default:
                return "id";
        }
    }

    private String getOrder(Map<String, Object> params) {
        String param = String.valueOf(params.getOrDefault(ORDER, "DESC"));
        return param.toUpperCase().equals("ASC") ? "ASC" : "DESC";
    }

    private int getLimit(Map<String, Object> params) {
        int result = Integer.parseInt(params.getOrDefault("limit",
                properties.getPageable().getDefaultPageSize()).toString());
        return result < properties.getPageable().getMaxPageSize() ? result : properties.getPageable().getMaxPageSize();
    }
}
