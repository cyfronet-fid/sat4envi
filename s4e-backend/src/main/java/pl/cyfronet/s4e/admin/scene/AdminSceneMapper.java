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

package pl.cyfronet.s4e.admin.scene;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.locationtech.jts.geom.Geometry;
import org.mapstruct.Mapper;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.config.MapStructCentralConfig;
import pl.cyfronet.s4e.util.GeometryUtil;

@Mapper(config = MapStructCentralConfig.class)
@Slf4j
public abstract class AdminSceneMapper {
    @Autowired
    private GeometryUtil geometryUtil;

    public abstract AdminSceneResponse projectionToResponse(AdminSceneProjection sceneProjection);

    protected AdminSceneResponse.FootprintPart geometryToFootprintPart(Geometry geometry3857) {
        if (geometry3857 == null) {
            return null;
        }

        val footprintPart = new AdminSceneResponse.FootprintPart();

        footprintPart.setEpsg3857(geometryUtil.toWkt(geometry3857));
        try {
            Geometry geometry4326 = geometryUtil.transform(geometry3857, "EPSG:3857", "EPSG:4326");
            footprintPart.setEpsg4326(geometryUtil.toWkt(geometry4326));
        } catch (FactoryException | TransformException e) {
            log.warn("Cannot transform geometry to EPSG:4326: '" + geometryUtil.toWkt(geometry3857) + "'", e);
        }

        return footprintPart;
    }
}
