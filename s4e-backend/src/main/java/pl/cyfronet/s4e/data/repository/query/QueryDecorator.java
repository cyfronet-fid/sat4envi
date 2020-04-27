package pl.cyfronet.s4e.data.repository.query;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class QueryDecorator implements QueryBuilder {
    private final QueryBuilder queryBuilder;

    protected abstract void doPrepareQueryAndParameters(Map<String, Object> params,
                                                     List<Object> parameters,
                                                     StringBuilder resultQuery);

    @Override
    public final void prepareQueryAndParameters(Map<String, Object> params,
                                                List<Object> parameters,
                                                StringBuilder resultQuery) {
        queryBuilder.prepareQueryAndParameters(params, parameters, resultQuery);
        doPrepareQueryAndParameters(params, parameters, resultQuery);
    }
}
