package pl.cyfronet.s4e.db;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestGeometryHelper;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@BasicTest
public class DbTimestampTest {
    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestGeometryHelper geom;

    @BeforeEach
    public void beforeEach() {
        sceneRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    public void shouldSaveTimestampWithoutDSTCorrections() {
        val product = productRepository.save(Product.builder()
                .name("testProductType")
                .displayName("testProductType")
                .layerName("testLayerName")
                .build());

        // A datetime, which if Polish timezone is used gets shifted one hour forward
        // on writing to DB.
        LocalDateTime dstBorderTimestamp = LocalDateTime.of(2019, 3, 31, 2, 0);
        val scene = sceneRepository.save(Scene.builder()
                .product(product)
                .timestamp(dstBorderTimestamp)
                .s3Path("some/path")
                .granulePath("mailto://bucket/some/path")
                .footprint(geom.any())
                .build());

        val sceneId = sceneRepository.save(scene).getId();

        val retrievedScene = sceneRepository.findById(sceneId).get();
        assertThat(retrievedScene.getTimestamp(), is(equalTo(dstBorderTimestamp)));
    }
}
