package pl.cyfronet.s4e.admin.scene;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import pl.cyfronet.s4e.bean.Legend;

import java.time.LocalDateTime;

@Data
class AdminSceneResponse {
    @Data
    public static class ProductPart {
        private Long id;
        private String name;
    }

    @Data
    public static class FootprintPart {
        private String epsg3857;
        private String epsg4326;
    }

    private Long id;
    private ProductPart product;
    private String sceneKey;
    private LocalDateTime timestamp;
    private String s3Path;
    private String granulePath;
    private FootprintPart footprint;
    private Legend legend;
    private JsonNode sceneContent;
    private JsonNode metadataContent;
}
