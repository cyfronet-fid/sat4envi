package pl.cyfronet.s4e.sync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import pl.cyfronet.s4e.IntegrationTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.TestResourceHelper;
import pl.cyfronet.s4e.data.repository.SceneRepository;

import java.nio.charset.StandardCharsets;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static pl.cyfronet.s4e.sync.SceneAcceptorTestHelper.SCENE_KEY;

/*
    The QueueConfig is not loaded in other tests.
    This is to avoid having registered queue listeners in other application contexts, which
    would intercept the tested message and make this test fail.
    ~ Jakub Sawicki
 */
@IntegrationTest
@TestPropertySource(properties = {
        "amqp.enabled=true",
        "amqp.create-queues=true",
        "s3.bucket=scene-acceptor-test"
})
public class QueueReceiverIntegrationTest {
    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private SceneAcceptorTestHelper sceneAcceptorTestHelper;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private TestResourceHelper testResourceHelper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private String incomingQueueName;

    private Long productId;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();

        productId = sceneAcceptorTestHelper.setUpProduct();
    }

    @ParameterizedTest
    @ValueSource(strings = { "s3:ObjectCreated:Put", "s3:ObjectCreated:Post" })
    public void shouldHandlePutAndPost(String eventName) {
        assertThat(sceneRepository.count(), is(equalTo(0L)));

        sendMessage(incomingQueueName, SCENE_KEY, eventName);

        await().until(() -> sceneRepository.findAllByProductId(productId), hasSize(1));
    }

    @Test
    public void shouldHandleDelete() {
        assertThat(sceneRepository.count(), is(equalTo(0L)));

        sendMessage(incomingQueueName, SCENE_KEY, "s3:ObjectCreated:Put");

        await().until(() -> sceneRepository.findAllByProductId(productId), hasSize(1));

        sendMessage(incomingQueueName, SCENE_KEY, "s3:ObjectRemoved:Delete");

        await().until(() -> sceneRepository.findAllByProductId(productId), hasSize(0));
    }

    private void sendMessage(String routingKey, String sceneKey, String eventName) {
        String payload = new String(testResourceHelper.getAsBytes("classpath:amqp/payload.json"), StandardCharsets.UTF_8)
                .replace("{eventName}", eventName)
                .replace("{sceneKey}", sceneKey);
        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        Message message = new Message(payload.getBytes(StandardCharsets.UTF_8), props);
        rabbitTemplate.send(routingKey, message);
    }
}
