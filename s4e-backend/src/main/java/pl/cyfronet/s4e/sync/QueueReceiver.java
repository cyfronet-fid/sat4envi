package pl.cyfronet.s4e.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

@RequiredArgsConstructor
@Slf4j
public class QueueReceiver {
    private final SceneAcceptor sceneAcceptor;

    @RabbitListener(queues = "#{incomingQueueName}")
    public void handle(Notification notification) {
        log.debug("Received notification: objectKey='{}', eventName='{}'", notification.getObjectKey(), notification.getEventName());
        Error error = sceneAcceptor.accept(notification.getObjectKey());
        if (error != null) {
            throw new AmqpRejectAndDontRequeueException(error.getCode());
        }
    }
}
