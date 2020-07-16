package pl.cyfronet.s4e.data.repository.query;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class QueryDecorator implements QueryBuilder {
    public static final String DATE_FORMAT = "YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"";
    private final QueryBuilder queryBuilder;

    protected abstract void doPrepareQueryAndParameters(Map<String, Object> params,
                                                        List<Object> parameters,
                                                        StringBuilder resultQuery,
                                                        Errors errors);

    @Override
    public final void prepareQueryAndParameters(Map<String, Object> params,
                                                List<Object> parameters,
                                                StringBuilder resultQuery,
                                                Errors errors) {
        queryBuilder.prepareQueryAndParameters(params, parameters, resultQuery, errors);
        doPrepareQueryAndParameters(params, parameters, resultQuery, errors);
    }
}
