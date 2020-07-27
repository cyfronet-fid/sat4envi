package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pl.cyfronet.s4e.SceneTestHelper.productBuilder;
import static pl.cyfronet.s4e.SceneTestHelper.sceneBuilder;

@BasicTest
@Slf4j
public class SceneServiceTest {
    private interface SceneProjection {
        Long getId();
    }

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private SceneService sceneService;

    private Product product;

    @BeforeEach
    public void setUp() {
        testDbHelper.clean();
        product = productRepository.save(productBuilder().build());
    }

    @Nested
    class ListWithTimestampRange {
        @Test
        public void shouldReturnFilteredScenes() throws NotFoundException {
            val scenes = Stream.of(
                    LocalDateTime.of(2019, 10, 11, 0, 0),
                    LocalDateTime.of(2019, 10, 11, 1, 0),
                    LocalDateTime.of(2019, 10, 12, 0, 0)
            )
                    .map(timestamp -> sceneBuilder(product).timestamp(timestamp).build())
                    .collect(Collectors.toList());
            sceneRepository.saveAll(scenes);

            List<SceneProjection> returned = sceneService.list(product.getId(), scenes.get(0).getTimestamp(), scenes.get(2).getTimestamp(), SceneProjection.class);

            assertThat(returned.stream().map(SceneProjection::getId).collect(Collectors.toList()), contains(scenes.get(0).getId(),scenes.get(1).getId()));
        }

        @Test
        public void shouldThrowNFEIfProductDoesntExist() {
            assertThrows(
                    NotFoundException.class,
                    () -> sceneService.list(product.getId() + 1, null, null, SceneProjection.class)
            );
        }
    }

    @Nested
    class Save {
        @Test
        public void shouldWork() {
            Scene scene = sceneBuilder(product).build();

            assertThat(sceneRepository.count(), is(equalTo(0L)));

            sceneService.save(scene);

            assertThat(sceneRepository.count(), is(equalTo(1L)));
        }
    }
}
