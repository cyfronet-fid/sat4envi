package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Webhook;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.util.S3Util;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@BasicTest
@Slf4j
public class SceneServiceTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SceneRepository sceneRepository;

    private SceneService sceneService;

    @Mock
    private S3Util s3Util;

    private static String WEBHOOK_KEY = "s4e-test-1/test/201810042345_Merkator_WV-IR.tif";

    @BeforeEach
    public void setUp() {
        sceneService = new SceneService(sceneRepository, productRepository, s3Util);
        sceneRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    public void shouldReturnFilteredScenes() {
        Product product = Product.builder()
                .name("testProductType")
                .build();
        productRepository.save(product);

        val scenes = List.of(
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName1")
                        .timestamp(LocalDateTime.of(2019, 10, 11, 0, 0))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName2")
                        .timestamp(LocalDateTime.of(2019, 10, 11, 1, 0))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .product(product)
                        .layerName("testLayerName3")
                        .timestamp(LocalDateTime.of(2019, 10, 12, 0, 0))
                        .s3Path("some/path")
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
                .build();
        productRepository.save(product);

        Scene scene = Scene.builder()
                .product(product)
                .layerName("testLayerName")
                .timestamp(LocalDateTime.now())
                .s3Path("some/path")
                .build();

        assertThat(sceneRepository.count(), is(equalTo(0L)));

        sceneService.saveScene(scene);

        assertThat(sceneRepository.count(), is(equalTo(1L)));
    }

    @Test
    public void shouldBuildFromWebhook() throws NotFoundException {
        Webhook webhook  = Webhook.builder()
                .eventName("EventName")
                .key(WEBHOOK_KEY)
                .build();
        Product product = Product.builder()
                .name("WV-IR")
                .build();
        productRepository.save(product);
        when(s3Util.getProduct(anyString())).thenReturn("WV-IR");

        Scene buildFromWebhook = sceneService.buildFromWebhook(webhook);
        assertThat(buildFromWebhook.getProduct().getName(), is(equalTo("WV-IR")));
    }

    @Test
    public void shouldReturnProduct() throws NotFoundException {
        Product product = Product.builder()
                .name("WV-IR")
                .build();
        productRepository.save(product);
        when(s3Util.getProduct(anyString())).thenReturn("WV-IR");
        Product fromDb = sceneService.getProduct(WEBHOOK_KEY);
        assertThat(fromDb.getName(), is(equalTo("WV-IR")));
    }

}
