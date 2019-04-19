package pl.cyfronet.s4e.service.request;

import lombok.Builder;
import lombok.Value;


/**
 * <pre>
 *     {
 *         "layer": {
 *             "defaultStyle": {
 *                 "name": "{defaultStyle}"
 *             }
 *         }
 *     }
 * </pre>
 */
@Value
public class SetLayerDefaultStyleRequest {
    @Value
    @Builder
    private static class DefaultStyle {
        String name;
    }

    @Value
    @Builder
    private static class Layer {
        DefaultStyle defaultStyle;
    }

    Layer layer;

    public SetLayerDefaultStyleRequest(String workspace, String defaultStyle) {
        layer = Layer.builder()
                .defaultStyle(DefaultStyle.builder()
                        .name(workspace+":"+defaultStyle)
                        .build())
                .build();
    }
}
