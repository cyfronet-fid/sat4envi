package pl.cyfronet.s4e.sync;

import lombok.Builder;
import lombok.Value;
import org.locationtech.jts.geom.Geometry;

import javax.json.JsonObject;

@Builder
@Value
public class Prototype {
    Long productId;
    String sceneKey;
    JsonObject sceneJson;
    JsonObject metadataJson;
    Geometry footprint;
}
