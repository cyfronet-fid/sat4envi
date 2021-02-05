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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.SceneService;
import pl.cyfronet.s4e.sync.context.Context;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcher {
    // These events correspond to event types listed for example here:
    // https://docs.ceph.com/en/latest/radosgw/s3-notification-compatibility/.
    private static final Set<String> ACCEPT_EVENTS = Set.of("s3:ObjectCreated:Put", "s3:ObjectCreated:Post");
    private static final Set<String> DELETE_EVENTS = Set.of("s3:ObjectRemoved:Delete");

    private final SceneAcceptor sceneAcceptor;
    private final SceneService sceneService;
    private final ContextRecorder contextRecorder;
    private final Clock clock;

    public void dispatch(Notification notification) {
        if (notification == null) {
            log.warn("A null notification received, rejecting");
            throw new AmqpRejectAndDontRequeueException("A null notification received");
        }

        log.info(
                "Received notification: eventName='{}', objectKey='{}'",
                notification.getObjectKey(),
                notification.getEventName()
        );

        val eventName = notification.getEventName();
        val objectKey = notification.getObjectKey();

        Context context = new Context(objectKey);
        context.setEventName(eventName);
        context.setInitiatedByMethod("amqp");
        context.setReceivedAt(LocalDateTime.ofInstant(clock.instant(), ZoneId.of("UTC")));

        if (eventName == null || objectKey == null) {
            log.warn("A notification with null eventName or objectKey received, rejecting");
            contextRecorder.record(context, null);
            throw new AmqpRejectAndDontRequeueException("A notification with null eventName or objectKey received");
        }

        if (ACCEPT_EVENTS.contains(eventName)) {
            accept(context);
        } else if (DELETE_EVENTS.contains(eventName)) {
            delete(context);
        } else {
            unknown(context);
        }
    }

    private void accept(Context context) {
        Error error = sceneAcceptor.accept(context);
        contextRecorder.record(context, error);
        if (error != null) {
            throw new AmqpRejectAndDontRequeueException(error.getCode());
        }
    }

    private void delete(Context context) {
        try {
            sceneService.deleteBySceneKey(context.getScene().getKey());
            contextRecorder.record(context, null);
        } catch (NotFoundException e) {
            log.warn("An unsupported attempt to delete a non-existent scene: sceneKey='{}'", context.getScene().getKey());
        }
    }

    private void unknown(Context context) {
        String message = "Notification with unsupported eventName: " +
                "eventName='" + context.getEventName() + "', objectKey='" + context.getScene().getKey() + "'";
        log.warn(message + ", rejecting");
        contextRecorder.record(context, context.getError().code("unsupported_event_name").build());
        throw new AmqpRejectAndDontRequeueException(message);
    }
}
