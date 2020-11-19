package pl.cyfronet.s4e.data.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final PreparedStatementBuilder preparedStatementBuilder;
    private Connection connection;

    public SceneRepositoryImpl(JdbcTemplate jdbcTemplate,
                               PreparedStatementBuilder preparedStatementBuilder)
            throws SQLException {
        this.jdbcTemplate = jdbcTemplate;
        this.preparedStatementBuilder = preparedStatementBuilder;
        this.connection = jdbcTemplate.getDataSource().getConnection();
    }

    public List<MappedScene> findAllByParamsMap(Map<String, Object> params) throws SQLException, QueryException {
        PreparedStatement preparedStatement = prepareQuery(params);
        try {
            return jdbcTemplate.query(connection -> preparedStatement, new SceneRowMapper());
        } catch (DataAccessException e) {
            MapBindingResult mapBindingResult = new MapBindingResult(new HashMap<>(), "params");
            mapBindingResult.addError(
                    new ObjectError("params", "Cannot execute query" + e.getMessage()));
            throw new QueryException(mapBindingResult);
        }
    }

    public Long countAllByParamsMap(Map<String, Object> params) throws SQLException, QueryException {
        PreparedStatement preparedStatement = prepareCountQuery(params);
        try {
            return jdbcTemplate.query(connection -> preparedStatement, (rs, rowNum) -> rs.getLong(1)).get(0);
        } catch (DataAccessException e) {
            MapBindingResult mapBindingResult = new MapBindingResult(new HashMap<>(), "params");
            mapBindingResult.addError(
                    new ObjectError("params", "Cannot execute query" + e.getMessage()));
            throw new QueryException(mapBindingResult);
        }
    }

    private PreparedStatement prepareQuery(Map<String, Object> params) throws SQLException, QueryException {
        PreparedStatement ps = preparedStatementBuilder.preparedStatement(connection, params);
        log.trace(ps.toString());
        return ps;
    }

    private PreparedStatement prepareCountQuery(Map<String, Object> params) throws SQLException, QueryException {
        PreparedStatement ps = preparedStatementBuilder.preparedCountStatement(connection, params);
        log.trace(ps.toString());
        return ps;
    }
}