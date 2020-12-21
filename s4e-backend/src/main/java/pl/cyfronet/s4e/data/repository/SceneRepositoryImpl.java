package pl.cyfronet.s4e.data.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;
import pl.cyfronet.s4e.api.MappedScene;
import pl.cyfronet.s4e.data.repository.query.PreparedStatementBuilder;
import pl.cyfronet.s4e.ex.QueryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * List of parameters in search:
 * Compiled from search on imgw instance of copernicus
 * limit [default 25, max = 1000]
 * offset [defaut 25]
 * order [ASC - DESC]
 * sortBy [id - ingestionTime - sensingTime]
 * timeZone -> ZoneParameter [client time]
 * sensingFrom
 * sensingTo
 * ingestionFrom
 * ingestionTo
 * satellitePlatform [STRING]
 * productType [STRING]
 * polarisation [STRING]
 * cloudCover [FLOAT]
 * processingLevel [STRING]
 * sensorMode [STRING]
 * collection [STRING]
 * timeliness [STRING]
 * instrument [STRING]
 * productLevel [STRING]
 * relativeOrbitNumber [STRING]
 * absoluteOrbitNumber [STRING]
 */
@Slf4j
public class SceneRepositoryImpl implements SceneRepositoryExt {
    private final JdbcTemplate jdbcTemplate;
    private final PreparedStatementBuilder.PrepareStatement prepareFindAllStatement;
    private final PreparedStatementBuilder.PrepareStatement prepareCountStatement;

    public SceneRepositoryImpl(JdbcTemplate jdbcTemplate, PreparedStatementBuilder preparedStatementBuilder) {
        this.jdbcTemplate = jdbcTemplate;
        this.prepareFindAllStatement = preparedStatementBuilder::preparedStatement;
        this.prepareCountStatement = preparedStatementBuilder::preparedCountStatement;
    }

    public List<MappedScene> findAllByParamsMap(Map<String, Object> params) throws QueryException {
        return query(
                connection -> prepare(connection, params, prepareFindAllStatement),
                new SceneRowMapper()
        );
    }

    public Long countAllByParamsMap(Map<String, Object> params) throws QueryException {
        List<Long> results = query(
                connection -> prepare(connection, params, prepareCountStatement),
                (rs, rowNum) -> rs.getLong(1)
        );

        if (results.size() == 0) {
            String paramsString = params.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .reduce((previous, keyValueString) -> previous + "," + keyValueString)
                    .orElse("<no params>");
            log.warn("A count call returned results with zero length, returning default (i.e. 0L). Params: " + paramsString + ".");
            return 0L;
        }

        return results.get(0);
    }

    private PreparedStatement prepare(
            Connection connection,
            Map<String, Object> params,
            PreparedStatementBuilder.PrepareStatement prepareStatement
    ) throws SQLException {
        try {
            PreparedStatement ps = prepareStatement.prepare(connection, params);
            log.trace(ps.toString());
            return ps;
        } catch (QueryException e) {
            // Wrap a checked exception in IAE, so that it can be thrown from a lambda.
            throw new IllegalArgumentException(e);
        }
    }

    private <T> List<T> query(PreparedStatementCreator psc, RowMapper<T> rowMapper) throws QueryException {
        try {
            return jdbcTemplate.query(psc, rowMapper);
        } catch (DataAccessException e) {
            MapBindingResult mapBindingResult = new MapBindingResult(new HashMap<>(), "params");
            mapBindingResult.addError(
                    new ObjectError("params", "Cannot execute query" + e.getMessage()));
            throw new QueryException(mapBindingResult);
        } catch (IllegalArgumentException e) {
            // Unwrap the wrapped QueryException.
            Throwable maybeQueryException = e.getCause();
            if (maybeQueryException instanceof QueryException) {
                throw (QueryException) maybeQueryException;
            } else {
                throw e;
            }
        }
    }
}
