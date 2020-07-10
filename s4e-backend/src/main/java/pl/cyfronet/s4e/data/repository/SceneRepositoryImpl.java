package pl.cyfronet.s4e.data.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import pl.cyfronet.s4e.api.MappedScene;
import pl.cyfronet.s4e.data.repository.query.PreparedStatementBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

    public List<MappedScene> findAllByParamsMap(Map<String, Object> params) {
        return jdbcTemplate.query(connection -> prepareQuery(params), new SceneRowMapper());
    }

    private PreparedStatement prepareQuery(Map<String, Object> params) throws SQLException {
        PreparedStatement ps = preparedStatementBuilder.preparedStatement(connection, params);
        log.trace(ps.toString());
        return ps;
    }

}