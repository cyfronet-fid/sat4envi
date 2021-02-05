/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
import pl.cyfronet.s4e.data.repository.SyncRecordRepository;

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
    private SyncRecordRepository syncRecordRepository;

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
        await().until(() -> syncRecordRepository.count() == 1L);
    }

    @ParameterizedTest
    @ValueSource(strings = { "s3:ObjectCreated:Put", "s3:ObjectCreated:Post" })
    public void shouldHandleNonExistentScene(String eventName) {
        assertThat(sceneRepository.count(), is(equalTo(0L)));

        sendMessage(incomingQueueName, "doesnt/exist.scene", eventName);

        await().until(() -> syncRecordRepository.count() == 1L);
        assertThat(sceneRepository.count(), is(equalTo(0L)));
    }

    @Test
    public void shouldHandleDelete() {
        assertThat(sceneRepository.count(), is(equalTo(0L)));

        sendMessage(incomingQueueName, SCENE_KEY, "s3:ObjectCreated:Put");

        await().until(() -> sceneRepository.findAllByProductId(productId), hasSize(1));
        await().until(() -> syncRecordRepository.count() == 1L);

        sendMessage(incomingQueueName, SCENE_KEY, "s3:ObjectRemoved:Delete");

        await().until(() -> sceneRepository.findAllByProductId(productId), hasSize(0));
        await().until(() -> syncRecordRepository.count() == 2L);
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
