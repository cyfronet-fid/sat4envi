package pl.cyfronet.s4e.data.repository.query;

import java.util.List;
import java.util.Map;

import static pl.cyfronet.s4e.search.SearchQueryParams.CLOUD_COVER;

public class QueryNumber extends QueryDecorator {
    public QueryNumber(QueryBuilder queryBuilder) {
        super(queryBuilder);
    }

    @Override
    public void doPrepareQueryAndParameters(Map<String, Object> params,
                                            List<Object> parameters,
                                            StringBuilder resultQuery) {
        queryByCloudCover(params, parameters, resultQuery);
    }

    private void queryByCloudCover(Map<String, Object> params,
                                   List<Object> parameters,
                                   StringBuilder resultQuery) {
        if (params.containsKey(CLOUD_COVER)) {
            resultQuery.append(" AND (metadata_content ->> 'cloud_cover')::float <= ? ");
            parameters.add(Float.parseFloat(params.get(CLOUD_COVER).toString()));
        }
    }
}
