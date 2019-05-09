package pl.cyfronet.s4e.geoserver.op.response;

import lombok.Data;

@Data
public class LayerResponse {
    @Data
    public static class DefaultStyle {
        String name;
    }

    @Data
    public static class Layer {
        private String name;
        private String type;
        private DefaultStyle defaultStyle;
    }

    private Layer layer;
}
