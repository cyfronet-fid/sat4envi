package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestGeometryHelper;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.util.TimeHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@BasicTest
@Slf4j
public class SceneServiceTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private TimeHelper timeHelper;

    @Autowired
    private TestGeometryHelper geom;

    private SceneService sceneService;

    @BeforeEach
    public void setUp() {
        sceneService = new SceneService(sceneRepository, timeHelper);
        sceneRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    public void shouldReturnFilteredScenes() {
        Product product = Product.builder()
                .name("testProductType")
                .displayName("testProductType")
                .layerName("test")
                .build();
        productRepository.save(product);

        val scenes = List.of(
                Scene.builder()
                        .product(product)
                        .timestamp(LocalDateTime.of(2019, 10, 11, 0, 0))
                        .s3Path("some/path")
                        .granulePath("mailto://bucket/some/path")
                        .footprint(geom.any())
                        .build(),
                Scene.builder()
                        .product(product)
                        .timestamp(LocalDateTime.of(2019, 10, 11, 1, 0))
                        .s3Path("some/path")
                        .granulePath("mailto://bucket/some/path")
                        .footprint(geom.any())
                        .build(),
                Scene.builder()
                        .product(product)
                        .timestamp(LocalDateTime.of(2019, 10, 12, 0, 0))
                        .s3Path("some/path")
                        .granulePath("mailto://bucket/some/path")
                        .footprint(geom.any())
                        .build()
        );
        sceneRepository.saveAll(scenes);

        List<Scene> returned = sceneService.getScenes(product.getId(), scenes.get(0).getTimestamp(), scenes.get(2).getTimestamp());

        assertThat(returned.stream().map(Scene::getId).collect(Collectors.toList()), contains(scenes.get(0).getId(),scenes.get(1).getId()));
    }

    @Test
    public void shouldSaveScene(){
        Product product = Product.builder()
                .name("testProductType")
                .displayName("testProductType")
                .layerName("test")
                .build();
        productRepository.save(product);

        Scene scene = Scene.builder()
                .product(product)
                .timestamp(LocalDateTime.now())
                .s3Path("some/path")
                .granulePath("mailto://bucket/some/path")
                .footprint(geom.any())
                .build();

        assertThat(sceneRepository.count(), is(equalTo(0L)));

        sceneService.saveScene(scene);

        assertThat(sceneRepository.count(), is(equalTo(1L)));
    }

}
