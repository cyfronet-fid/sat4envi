package pl.cyfronet.s4e.admin.scene;

import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.Geometry;
import pl.cyfronet.s4e.bean.Legend;

import java.time.LocalDateTime;

public interface AdminSceneProjection {
    interface ProductProjection {
        Long getId();
        String getName();
    }

    Long getId();
    ProductProjection getProduct();
    String getSceneKey();
    LocalDateTime getTimestamp();
    String getS3Path();
    String getGranulePath();
    Geometry getFootprint();
    Legend getLegend();
    JsonNode getSceneContent();
    JsonNode getMetadataContent();
}
