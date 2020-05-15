package pl.cyfronet.s4e.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import pl.cyfronet.s4e.properties.AmqpProperties;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
@Slf4j
public class QueueCreator {
    private final AmqpAdmin amqpAdmin;
    private final AmqpProperties amqpProperties;

    @PostConstruct
    public void createQueues() {
        if (amqpProperties.isCreateQueues()) {
            String incomingQueueName = amqpProperties.getQueues().getIncoming();
            log.info("Creating queue: {}", incomingQueueName);
            doCreateQueue(incomingQueueName);
        } else {
            log.info("Skipping queue creation, disabled by a property");
        }
    }

    private void doCreateQueue(String queueName) {
        amqpAdmin.declareQueue(getQueue(queueName));
    }

    private Queue getQueue(String name) {
        return new Queue(name, false);
    }
}
