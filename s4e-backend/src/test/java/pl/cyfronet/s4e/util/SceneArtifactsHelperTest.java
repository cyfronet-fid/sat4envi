package pl.cyfronet.s4e.util;

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

import javax.validation.ConstraintViolationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@BasicTest
class SceneArtifactsHelperTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private SceneArtifactsHelper sceneArtifactsHelper;

    @Autowired
    private TestDbHelper testDbHelper;

    private Scene scene;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();

        Product product = productRepository.save(SceneTestHelper.productBuilder().build());
        scene = sceneRepository.save(SceneTestHelper.sceneBuilder(product).build());
    }

    @Test
    public void shouldWork() throws NotFoundException {
        assertThat(sceneArtifactsHelper.getArtifact(scene.getId(), "default_artifact"), is(equalTo("some/path")));
        assertThat(sceneArtifactsHelper.getArtifact(scene.getId(), "other_artifact"), is(equalTo("some/other/path")));
    }

    @Test
    public void shouldProhibitNullArguments() {
        assertThrows(ConstraintViolationException.class, () -> sceneArtifactsHelper.getArtifact(scene.getId(), null));
        assertThrows(ConstraintViolationException.class, () -> sceneArtifactsHelper.getArtifact(scene.getId(), " "));
        assertThrows(ConstraintViolationException.class, () -> sceneArtifactsHelper.getArtifact(null, "some_artifact"));
        assertThrows(ConstraintViolationException.class, () -> sceneArtifactsHelper.getArtifact(null, null));
        assertThrows(ConstraintViolationException.class, () -> sceneArtifactsHelper.getArtifact(null, " "));
    }

    @Test
    public void shouldThrowNFEIfSceneDoesntExist() {
        assertThrows(NotFoundException.class, () -> sceneArtifactsHelper.getArtifact(scene.getId() + 1, "other_artifact"));
    }

    @Test
    public void shouldThrowNFEForNonExistentArtifact() {
        assertThrows(NotFoundException.class, () -> sceneArtifactsHelper.getArtifact(scene.getId(), "not_existent"));
    }
}
