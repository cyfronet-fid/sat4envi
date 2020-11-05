package pl.cyfronet.s4e.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.SceneService;

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

    public void dispatch(Notification notification) {
        if (notification == null) {
            return;
        }

        val eventName = notification.getEventName();
        val objectKey = notification.getObjectKey();

        if (ACCEPT_EVENTS.contains(eventName)) {
            accept(objectKey);
        } else if (DELETE_EVENTS.contains(eventName)) {
            delete(objectKey);
        } else {
            log.warn(
                    "Notification with unsupported eventName: objectKey='{}', eventName='{}'",
                    objectKey,
                    eventName
            );
        }
    }

    private void accept(String sceneKey) {
        Error error = sceneAcceptor.accept(sceneKey);
        if (error != null) {
            throw new AmqpRejectAndDontRequeueException(error.getCode());
        }
    }

    private void delete(String sceneKey) {
        try {
            sceneService.deleteBySceneKey(sceneKey);
        } catch (NotFoundException e) {
            log.warn("An unsupported attempt to delete a non-existent scene: sceneKey='{}'", sceneKey);
        }
    }
}
