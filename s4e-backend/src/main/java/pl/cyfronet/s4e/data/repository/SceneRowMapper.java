package pl.cyfronet.s4e.data.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import pl.cyfronet.s4e.api.MappedScene;
import pl.cyfronet.s4e.bean.Scene;

import java.sql.ResultSet;
import java.sql.SQLException;

@RequiredArgsConstructor
public class SceneRowMapper implements RowMapper<MappedScene> {
    @Override
    public MappedScene mapRow(ResultSet rs, int i) throws SQLException {
        return MappedScene.builder()
                .id(rs.getLong(Scene.COLUMN_ID))
                .productId(rs.getLong(Scene.COLUMN_PRODUCT_ID))
                .sceneKey(rs.getString(Scene.COLUMN_SCENE_KEY))
                .footprint(rs.getString(Scene.COLUMN_FOOTPRINT))
                .metadataContent(rs.getString(Scene.COLUMN_METADATA))
                .sceneContent(rs.getString(Scene.COLUMN_CONTENT))
                .timestamp(rs.getTimestamp(Scene.COLUMN_TIMESTAMP).toLocalDateTime())
                .build();
    }
}
