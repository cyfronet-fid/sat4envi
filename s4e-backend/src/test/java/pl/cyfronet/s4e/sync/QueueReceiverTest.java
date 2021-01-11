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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueueReceiverTest {
    @Mock
    private NotificationDispatcher notificationDispatcher;

    private QueueReceiver queueReceiver;

    @BeforeEach
    public void beforeEach() {
        queueReceiver = new QueueReceiver(notificationDispatcher);
    }

    @Test
    public void shouldRejectIfDispatcherThrowsRuntimeException() {
        Notification notification = mock(Notification.class);
        doThrow(RuntimeException.class).when(notificationDispatcher).dispatch(notification);

        assertRejects(notification);
    }

    @Test
    public void shouldRejectIfDispatcherRejects() {
        Notification notification = mock(Notification.class);
        doThrow(AmqpRejectAndDontRequeueException.class).when(notificationDispatcher).dispatch(notification);

        assertRejects(notification);
    }

    @Test
    public void shouldWork() {
        Notification notification = mock(Notification.class);

        queueReceiver.handle(notification);

        verify(notificationDispatcher).dispatch(notification);
    }

    private void assertRejects(Notification notification) {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> queueReceiver.handle(notification));
    }

}
