package pl.cyfronet.s4e.admin.product;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.Legend;
import pl.cyfronet.s4e.bean.Product;

import javax.validation.constraints.Pattern;
import java.util.Map;

@Data
@Builder
class AdminUpdateProductRequest {
    @Pattern(regexp = "[-_a-zA-Z0-9]+")
    private String name;

    private String displayName;

    private String description;

    private Product.AccessType accessType;

    private Legend legend;

    @Pattern(regexp = "[_a-z0-9]+")
    private String layerName;

    private String sceneSchemaName;

    private String metadataSchemaName;

    private Map<String, String> granuleArtifactRule;

    private String productCategoryName;
}
