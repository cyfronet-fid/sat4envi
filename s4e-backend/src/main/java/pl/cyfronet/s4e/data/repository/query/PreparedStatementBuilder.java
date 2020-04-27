package pl.cyfronet.s4e.data.repository.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class PreparedStatementBuilder {
    private final QueryBuilder queryBuilder;

    public PreparedStatement preparedStatement(Connection connection,
                                               Map<String, Object> params) throws SQLException {
        List<Object> parameters = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement(prepareQueryAndParameters(params, parameters));
        addParametersToStatement(ps, parameters);
        return ps;
    }

    private String prepareQueryAndParameters(Map<String, Object> params,
                                             List<Object> parameters) {
        StringBuilder resultQuery = new StringBuilder();
        queryBuilder.prepareQueryAndParameters(params, parameters, resultQuery);
        // [] inputAuxiliaryFiles -> ...
        // [] polygon -> field[footprint]
        return resultQuery.toString();
    }

    private void addParametersToStatement(PreparedStatement preparedStatement,
                                          List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            preparedStatement.setObject(i + 1, parameters.get(i));
        }
    }
}
