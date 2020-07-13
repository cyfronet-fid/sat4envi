package pl.cyfronet.s4e.admin.product;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.Legend;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.Map;

@Data
@Builder
class AdminCreateProductRequest {
    @NotEmpty
    @Pattern(regexp = "[-_a-zA-Z0-9]+")
    private String name;

    @NotEmpty
    private String displayName;

    @NotEmpty
    private String description;

    private Legend legend;

    @NotEmpty
    @Pattern(regexp = "[_a-z0-9]+")
    private String layerName;

    @NotEmpty
    private String sceneSchemaName;

    @NotEmpty
    private String metadataSchemaName;

    @NotEmpty
    private Map<String, String> granuleArtifactRule;
}
