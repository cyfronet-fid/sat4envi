package pl.cyfronet.s4e.sync;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.TestGeometryHelper;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.ex.NotFoundException;

import javax.json.JsonValue;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pl.cyfronet.s4e.SceneTestHelper.sceneBuilder;

@BasicTest
class ScenePersisterTest {
    @Autowired
    private ScenePersister scenePersister;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestDbHelper testDbHelper;

    private Faker faker = new Faker();

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();
    }

    @Test
    public void shouldSaveNewScene() throws NotFoundException {
        Product product = createProduct();
        Prototype prototype = getPrototype(product.getId(), "key/path/of/a.scene");

        assertThat(listProductScenes(product.getId()), hasSize(0));

        Long persistedId = scenePersister.persist(prototype);

        assertThat(listProductScenes(product.getId()), hasItems(allOf(
                hasProperty("id", equalTo(persistedId)),
                hasProperty("sceneKey", equalTo(prototype.getSceneKey()))
        )));
    }

    @Test
    public void shouldUpdateExistingScene() throws NotFoundException {
        Product product = createProduct();
        String sceneKey = "key/path/of/a.scene";
        Scene scene = createScene(product, sceneKey);
        Prototype prototype = getPrototype(product.getId(), sceneKey);

        assertThat(listProductScenes(product.getId()), hasItems(allOf(
                hasProperty("id", equalTo(scene.getId())),
                hasProperty("sceneKey", equalTo(sceneKey))
        )));

        Long persistedId = scenePersister.persist(prototype);

        assertThat(persistedId, is(equalTo(scene.getId())));
        assertThat(listProductScenes(product.getId()), hasItems(allOf(
                hasProperty("id", equalTo(persistedId)),
                hasProperty("sceneKey", equalTo(sceneKey)),
                hasProperty("s3Path", equalTo(prototype.getS3Path())),
                hasProperty("granulePath", equalTo("mailto://s4e-test-1/" + prototype.getS3Path()))
        )));
    }

    @Test
    public void shouldVerifyUpdatedSceneProductMatches() {
        Product product1 = createProduct();
        Product product2 = createProduct();
        String sceneKey = "key/path/of/a.scene";
        Scene scene = createScene(product1, sceneKey);
        Prototype prototype = getPrototype(product2.getId(), sceneKey);

        assertThat(listProductScenes(product1.getId()), hasItems(allOf(
                hasProperty("id", equalTo(scene.getId())),
                hasProperty("sceneKey", equalTo(sceneKey))
        )));
        assertThat(listProductScenes(product2.getId()), is(empty()));

        assertThrows(IllegalArgumentException.class, () -> scenePersister.persist(prototype));

        assertThat(listProductScenes(product1.getId()), hasItems(allOf(
                hasProperty("id", equalTo(scene.getId())),
                hasProperty("sceneKey", equalTo(sceneKey))
        )));
        assertThat(listProductScenes(product2.getId()), is(empty()));
    }

    private Product createProduct() {
        String displayName = faker.numerify(faker.space().constellation() + " ##");
        String name = displayName.replace(" ", "_");
        return productRepository.save(Product.builder()
                .name(name)
                .displayName(displayName)
                .layerName(name.toLowerCase())
                .build());
    }

    private Scene createScene(Product product, String sceneKey) {
        return sceneRepository.save(sceneBuilder(product)
                .sceneKey(sceneKey)
                .build());
    }

    private Prototype getPrototype(Long productId, String sceneKey) {
        return Prototype.builder()
                .sceneKey(sceneKey)
                .s3Path("path/to/granule.tif")
                .productId(productId)
                .timestamp(LocalDateTime.now())
                .footprint(TestGeometryHelper.ANY_POLYGON)
                .sceneJson(JsonValue.EMPTY_JSON_OBJECT)
                .metadataJson(JsonValue.EMPTY_JSON_OBJECT)
                .build();
    }

    private List<Scene> listProductScenes(Long productId) {
        return sceneRepository.findAllByProductId(productId);
    }

}
