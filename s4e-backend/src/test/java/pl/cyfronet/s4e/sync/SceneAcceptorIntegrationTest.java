package pl.cyfronet.s4e.sync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import pl.cyfronet.s4e.IntegrationTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.TestResourceHelper;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Schema;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.data.repository.SchemaRepository;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@IntegrationTest
@TestPropertySource(properties = {
        "s3.bucket=scene-acceptor-test"
})
class SceneAcceptorIntegrationTest {
    private static final String SCENE_SCHEMA_PATH = "classpath:schema/Sentinel-1.scene.v1.json";
    private static final String METADATA_SCHEMA_PATH = "classpath:schema/Sentinel-1.metadata.v1.json";

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private SceneAcceptor sceneAcceptor;

    @Autowired
    private TestResourceHelper testResourceHelper;

    @Autowired
    private SchemaRepository schemaRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SceneRepository sceneRepository;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();
    }

    @Test
    public void test() {
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

        productRepository.save(Product.builder()
                .name("Sentinel-1-GRDH")
                .displayName("Sentinel-1-GRDH")
                .layerName("sentinel_1_grdh")
                .sceneSchema(sceneSchema)
                .metadataSchema(metadataSchema)
                .granuleArtifactRule(Map.of("default", "quicklook", "COG", "RGBs_8b"))
                .build());

        assertThat(sceneRepository.count(), is(equalTo(0L)));

        String sceneKey = "/Sentinel-1/GRDH/2020-02-28/S1A_IW_GRDH_1SDV_20200228T045117_20200228T045142_031448_039EDF_82C8.scene";
        sceneAcceptor.accept(sceneKey);

        assertThat(sceneRepository.count(), is(equalTo(1L)));
    }
}
