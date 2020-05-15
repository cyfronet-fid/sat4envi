package pl.cyfronet.s4e.sync;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.TestResourceHelper;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Schema;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SchemaRepository;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
class SceneAcceptorTestHelper {
    public static final String SCENE_SCHEMA_PATH = "classpath:schema/Sentinel-1.scene.v1.json";
    public static final String METADATA_SCHEMA_PATH = "classpath:schema/Sentinel-1.metadata.v1.json";
    public static final String SCENE_KEY = "Sentinel-1/GRDH/2020-02-28/S1A_IW_GRDH_1SDV_20200228T045117_20200228T045142_031448_039EDF_82C8.scene";

    private final SchemaRepository schemaRepository;
    private final ProductRepository productRepository;
    private final TestResourceHelper testResourceHelper;

    public Long setUpProduct() {
        Schema sceneSchema = schemaRepository.save(Schema.builder()
                .name("Sentinel-1.scene.v1.json")
                .type(Schema.Type.SCENE)
                .content(new String(testResourceHelper.getAsBytes(SCENE_SCHEMA_PATH), StandardCharsets.UTF_8))
                .build());

        Schema metadataSchema = schemaRepository.save(Schema.builder()
                .name("Sentinel-1.metadata.v1.json")
                .type(Schema.Type.METADATA)
                .content(new String(testResourceHelper.getAsBytes(METADATA_SCHEMA_PATH), StandardCharsets.UTF_8))
                .build());

        return productRepository.save(Product.builder()
                .name("Sentinel-1-GRDH")
                .displayName("Sentinel-1-GRDH")
                .layerName("sentinel_1_grdh")
                .sceneSchema(sceneSchema)
                .metadataSchema(metadataSchema)
                .granuleArtifactRule(Map.of("default", "quicklook", "COG", "RGBs_8b"))
                .build()).getId();
    }
}
