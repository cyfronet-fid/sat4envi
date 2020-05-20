package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.util.TimeHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static pl.cyfronet.s4e.SceneTestHelper.productBuilder;
import static pl.cyfronet.s4e.SceneTestHelper.sceneBuilder;

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
    private TestDbHelper testDbHelper;

    @Autowired
    private SceneService sceneService;

    @BeforeEach
    public void setUp() {
        testDbHelper.clean();
    }

    @Test
    public void shouldReturnFilteredScenes() {
        Product product = productRepository.save(productBuilder().build());

        val scenes = Stream.of(
                LocalDateTime.of(2019, 10, 11, 0, 0),
                LocalDateTime.of(2019, 10, 11, 1, 0),
                LocalDateTime.of(2019, 10, 12, 0, 0)
        )
                .map(timestamp -> sceneBuilder(product).timestamp(timestamp).build())
                .collect(Collectors.toList());
        sceneRepository.saveAll(scenes);

        List<Scene> returned = sceneService.getScenes(product.getId(), scenes.get(0).getTimestamp(), scenes.get(2).getTimestamp());

        assertThat(returned.stream().map(Scene::getId).collect(Collectors.toList()), contains(scenes.get(0).getId(),scenes.get(1).getId()));
    }

    @Test
    public void shouldSaveScene() {
        Product product = productRepository.save(productBuilder().build());

        Scene scene = sceneBuilder(product).build();

        assertThat(sceneRepository.count(), is(equalTo(0L)));

        sceneService.saveScene(scene);

        assertThat(sceneRepository.count(), is(equalTo(1L)));
    }
}
