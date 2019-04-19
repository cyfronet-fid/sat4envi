package pl.cyfronet.s4e.service.request;

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
public class CreateStyleRequest {
    @Value
    @Builder
    private static class Style {
        String namespace;
        String name;
        String filename;
    }

    Style style;

    public CreateStyleRequest(String workspace, String style) {
        this.style = Style.builder()
                .namespace(workspace)
                .name(style)
                .filename(style+".sld")
                .build();
    }
}
