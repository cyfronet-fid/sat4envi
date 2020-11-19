package pl.cyfronet.s4e.data.repository.query;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.Map;

import static pl.cyfronet.s4e.search.SearchQueryParams.*;

public class QueryEnding extends QueryDecorator {
    private final SpringDataWebProperties properties;

    public QueryEnding(QueryBuilder queryBuilder, SpringDataWebProperties properties) {
        super(queryBuilder);
        this.properties = properties;
    }

    @Override
    public void doPrepareQueryAndParameters(Map<String, Object> params,
                                            List<Object> parameters,
                                            StringBuilder resultQuery,
                                            Errors errors) {
        resultQuery.append(" ORDER BY " + getOrderByField(params, errors) + " " + getOrder(params));
        resultQuery.append(" LIMIT ? ");
        parameters.add(getLimit(params, errors));
        resultQuery.append(" OFFSET ? ;");
        parameters.add(getOffset(params, errors));
    }

    @Override
    protected void doPrepareCountQueryAndParameters(Map<String, Object> params, List<Object> parameters, StringBuilder resultQuery, Errors errors) {
        resultQuery.append(" ;");
    }

    private String getOrderByField(Map<String, Object> params,
                                   Errors errors) {
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

    private int getLimit(Map<String, Object> params,
                         Errors errors) {
        try {
            int result = Integer.parseInt(params.getOrDefault(LIMIT,
                    properties.getPageable().getDefaultPageSize()).toString());
            if (result < 0) {
                throw new NumberFormatException("Limit cannot be negative");
            }
            return result < properties.getPageable().getMaxPageSize() ? result : properties.getPageable().getMaxPageSize();
        } catch (NumberFormatException e) {
            errors.rejectValue(LIMIT, "pl.cyfronet.s4e.data.repository.query.QueryEnding.limit.message", e.getMessage());
            return 0;
        }
    }

    private int getOffset(Map<String, Object> params,
                          Errors errors) {
        try {
            int result = Integer.parseInt(params.getOrDefault(OFFSET, 0).toString());
            if (result < 0) {
                throw new NumberFormatException("Offset cannot be negative");
            }
            return result;
        } catch (NumberFormatException e) {
            errors.rejectValue(OFFSET, "pl.cyfronet.s4e.data.repository.query.QueryEnding.offset.message", e.getMessage());
            return properties.getPageable().getDefaultPageSize();
        }
    }
}
