package pl.cyfronet.s4e.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.SceneTestHelper;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static pl.cyfronet.s4e.SceneTestHelper.productBuilder;

@BasicTest
@Slf4j
public class SceneFileStorageServiceTest {
    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private SceneFileStorageService sceneFileStorageService;

    private Scene scene;

    @BeforeEach
    public void beforeEach() throws Exception {
        testDbHelper.clean();
        Product product = productRepository.save(productBuilder().build());
        //addscenewithmetadata
        scene = buildScene(product);
        scene.setSceneContent(objectMapper.readTree(SceneTestHelper.getSceneContent()));
        sceneRepository.save(scene);
    }

    @AfterEach
    public void afterEach() {
        testDbHelper.clean();
    }

    private Scene buildScene(Product product) {
        return SceneTestHelper.sceneBuilder(product).build();
    }

    @Test
    public void shouldReturnSceneArtifacts() throws NotFoundException {
        Map<String, String> result = sceneFileStorageService.getSceneArtifacts(scene.getId());

        assertThat(result.size(), is(equalTo(7)));
    }
}
