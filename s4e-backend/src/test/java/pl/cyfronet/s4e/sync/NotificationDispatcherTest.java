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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.SceneService;
import pl.cyfronet.s4e.sync.context.Context;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationDispatcherTest {
    @Mock
    private SceneAcceptor sceneAcceptor;

    @Mock
    private SceneService sceneService;

    @Mock
    private ContextRecorder contextRecorder;

    private NotificationDispatcher notificationDispatcher;

    @BeforeEach
    public void beforeEach() {
        notificationDispatcher = new NotificationDispatcher(sceneAcceptor, sceneService, contextRecorder, Clock.systemUTC());
    }

    @Test
    public void shouldRejectIfNotificationNull() {
        assertRejects(null);
    }

    @ParameterizedTest
    @CsvSource({
            ",",
            "eventNameValue,",
            ",objectKeyValue",
            ",objectKeyValue",
            "s3:ObjectCreated:Copy,objectKeyValue"
    })
    public void shouldRejectIfAnyNotificationFieldNullOrEventUnsupported(String eventName, String objectKey) {
        Notification notification = mockNotification(eventName, objectKey);

        assertRejects(notification);
    }

    @Test
    public void shouldRejectIfAcceptReturnsError() {
        Notification notification = mockNotification("s3:ObjectCreated:Put", "key");
        doReturn(Error.builder("key").build()).when(sceneAcceptor).accept(new Context("key"));

        assertRejects(notification);
    }

    @Test
    public void shouldPassIfDeleteThrowsNFE() throws NotFoundException {
        Notification notification = mockNotification("s3:ObjectRemoved:Delete", "key");
        doThrow(NotFoundException.class).when(sceneService).deleteBySceneKey("key");

        notificationDispatcher.dispatch(notification);
    }

    @Test
    public void shouldPass() {
        Notification notification = mockNotification("s3:ObjectCreated:Put", "key");

        notificationDispatcher.dispatch(notification);
    }

    private Notification mockNotification(String eventName, String objectKey) {
        Notification notification = mock(Notification.class);
        doReturn(eventName).when(notification).getEventName();
        doReturn(objectKey).when(notification).getObjectKey();
        return notification;
    }

    private void assertRejects(Notification notification) {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> notificationDispatcher.dispatch(notification));
    }
}
