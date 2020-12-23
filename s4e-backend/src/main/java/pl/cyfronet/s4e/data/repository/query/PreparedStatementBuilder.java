/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.cyfronet.s4e.data.repository.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.MapBindingResult;
import pl.cyfronet.s4e.ex.QueryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class PreparedStatementBuilder {
    public interface PrepareStatement {
        PreparedStatement prepare(Connection connection, Map<String, Object> params) throws SQLException, QueryException;
    }

    private final QueryBuilder queryBuilder;

    public PreparedStatement preparedStatement(Connection connection,
                                               Map<String, Object> params) throws SQLException, QueryException {
        List<Object> parameters = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement(prepareQueryAndParameters(params, parameters));
        addParametersToStatement(ps, parameters);
        return ps;
    }

    public PreparedStatement preparedCountStatement(Connection connection,
                                               Map<String, Object> params) throws SQLException, QueryException {
        List<Object> parameters = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement(prepareCountQueryAndParameters(params, parameters));
        addParametersToStatement(ps, parameters);
        return ps;
    }

    private String prepareQueryAndParameters(Map<String, Object> params,
                                             List<Object> parameters) throws QueryException {
        StringBuilder resultQuery = new StringBuilder();
        MapBindingResult errors = new MapBindingResult(new HashMap<>(), "params");
        queryBuilder.prepareQueryAndParameters(params, parameters, resultQuery, errors);
        if (errors.hasErrors()) {
            throw new QueryException(errors);
        }
        // [] inputAuxiliaryFiles -> ...
        // [] polygon -> field[footprint]
        return resultQuery.toString();
    }

    private String prepareCountQueryAndParameters(Map<String, Object> params,
                                                  List<Object> parameters) throws QueryException {
        StringBuilder resultQuery = new StringBuilder();
        MapBindingResult errors = new MapBindingResult(new HashMap<>(), "params");
        queryBuilder.prepareCountQueryAndParameters(params, parameters, resultQuery, errors);
        if (errors.hasErrors()) {
            throw new QueryException(errors);
        }
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
