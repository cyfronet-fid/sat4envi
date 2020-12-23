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
