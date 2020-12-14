package pl.cyfronet.s4e.controller.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.config.MapStructCentralConfig;
import pl.cyfronet.s4e.controller.response.SceneResponse;
import pl.cyfronet.s4e.util.GeometryUtil;
import pl.cyfronet.s4e.util.TimeHelper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pl.cyfronet.s4e.bean.Schema.SCENE_SCHEMA_ARTIFACTS_KEY;

@Mapper(config = MapStructCentralConfig.class)
@Slf4j
public abstract class SceneMapper {
    @Autowired
    private TimeHelper timeHelper;

    @Autowired
    private GeometryUtil geometryUtil;

    @Mapping(source = "scene", target = "productId", qualifiedByName = "productId")
    @Mapping(source = "sceneContent", target = "artifacts")
    public abstract SceneResponse toResponse(SceneResponse.Projection scene, @Context ZoneId zoneId);

    @Named("productId")
    protected Long getProductId(SceneResponse.Projection scene) {
        return scene.getProduct().getId();
    }

    protected ZonedDateTime mapToZonedDateTime(LocalDateTime timestamp, @Context ZoneId zoneId) {
        return timeHelper.getZonedDateTime(timestamp, zoneId);
    }

    protected String mapTo4326Wkt(Geometry footprint) {
        try {
            return geometryUtil.transform(footprint, "EPSG:3857", "EPSG:4326").toText();
        } catch (FactoryException | TransformException e) {
            log.warn("Cannot transform geometry to EPSG:4326: '" + footprint + "'", e);
            return null;
        }
    }

    protected Set<String> mapToArtifactNames(JsonNode sceneContent) {
        ObjectNode artifacts = (ObjectNode) sceneContent.get(SCENE_SCHEMA_ARTIFACTS_KEY);
        Iterator<String> artifactNamesIterator = artifacts.fieldNames();
        return Stream.generate(() -> null)
                .takeWhile(ignored -> artifactNamesIterator.hasNext())
                .map(ignored -> artifactNamesIterator.next())
                .collect(Collectors.toUnmodifiableSet());
    }
}
