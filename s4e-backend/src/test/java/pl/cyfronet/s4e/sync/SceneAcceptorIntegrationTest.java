package pl.cyfronet.s4e.sync;

import org.awaitility.Durations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import pl.cyfronet.s4e.IntegrationTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.data.repository.SceneRepository;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static pl.cyfronet.s4e.sync.SceneAcceptorTestHelper.SCENE_KEY;

@IntegrationTest
@TestPropertySource(properties = {
        "s3.bucket=scene-acceptor-test"
})
class SceneAcceptorIntegrationTest {
    @Autowired
    private SceneAcceptor sceneAcceptor;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private SceneAcceptorTestHelper sceneAcceptorTestHelper;

    @Autowired
    private TestDbHelper testDbHelper;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();
    }

    @Test
    public void test() {
        Long productId = sceneAcceptorTestHelper.setUpProduct();

        assertThat(sceneRepository.count(), is(equalTo(0L)));

        sceneAcceptor.accept(SCENE_KEY);

        await().atMost(Durations.TEN_SECONDS)
                .until(() -> sceneRepository.findAllByProductId(productId), hasSize(greaterThan(0)));
    }
}
