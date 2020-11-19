package pl.cyfronet.s4e.data.repository.query;

import org.springframework.validation.Errors;

import java.util.List;
import java.util.Map;

public interface QueryBuilder {
    void prepareQueryAndParameters(Map<String, Object> params,
                                   List<Object> parameters,
                                   StringBuilder resultQuery,
                                   Errors errors);

    void prepareCountQueryAndParameters(Map<String, Object> params,
                                        List<Object> parameters,
                                        StringBuilder resultQuery,
                                        Errors errors);
}
