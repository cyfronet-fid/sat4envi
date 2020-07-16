package pl.cyfronet.s4e.data.repository.query;

import org.springframework.validation.Errors;

import java.util.List;
import java.util.Map;

import static pl.cyfronet.s4e.search.SearchQueryParams.CLOUD_COVER;

public class QueryNumber extends QueryDecorator {
    public static final int MAX_CLOUD_COVERAGE = 100;
    public static final int MIN_CLOUD_COVERAGE = 0;

    public QueryNumber(QueryBuilder queryBuilder) {
        super(queryBuilder);
    }

    @Override
    public void doPrepareQueryAndParameters(Map<String, Object> params,
                                            List<Object> parameters,
                                            StringBuilder resultQuery,
                                            Errors errors) {
        queryByCloudCover(params, parameters, resultQuery, errors);
    }

    private void queryByCloudCover(Map<String, Object> params,
                                   List<Object> parameters,
                                   StringBuilder resultQuery,
                                   Errors errors) {
        if (params.containsKey(CLOUD_COVER)) {
            resultQuery.append(" AND (metadata_content ->> 'cloud_cover')::float <= ? ");
            parameters.add(getCloudCover(params, errors));
        }
    }

    private float getCloudCover(Map<String, Object> params,
                                Errors errors) {
        try {
            float result = Float.parseFloat(params.get(CLOUD_COVER).toString());
            if (result < MIN_CLOUD_COVERAGE || result > MAX_CLOUD_COVERAGE) {
                throw new NumberFormatException("Cloud cover range is [0 - 100]");
            }
            return result;
        } catch (NumberFormatException e) {
            errors.rejectValue(CLOUD_COVER, "pl.cyfronet.s4e.data.repository.query.QueryNumber.message", e.getMessage());
            return 0;
        }
    }
}
