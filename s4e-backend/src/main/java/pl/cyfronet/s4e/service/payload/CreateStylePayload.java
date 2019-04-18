package pl.cyfronet.s4e.service.payload;

import lombok.Builder;
import lombok.Value;


/**
 *  <pre>
 *      {
 *          "style": {
 *              "namespace": "{workspace}",
 *              "name": "{style}",
 *              "filename": "{style}.sld"
 *          }
 *      }
 * </pre>
 */
@Value
public class CreateStylePayload {
    @Value
    @Builder
    private static class Style {
        String namespace;
        String name;
        String filename;
    }

    Style style;

    public CreateStylePayload(String workspace, String style) {
        this.style = Style.builder()
                .namespace(workspace)
                .name(style)
                .filename(style+".sld")
                .build();
    }
}
