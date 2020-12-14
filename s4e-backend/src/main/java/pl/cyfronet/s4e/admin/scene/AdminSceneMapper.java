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

        footprintPart.setEpsg3857(geometry3857.toText());
        try {
            Geometry geometry4326 = geometryUtil.transform(geometry3857, "EPSG:3857", "EPSG:4326");
            footprintPart.setEpsg4326(geometry4326.toText());
        } catch (FactoryException | TransformException e) {
            log.warn("Cannot transform geometry to EPSG:4326: '" + geometry3857.toText() + "'", e);
        }

        return footprintPart;
    }
}
