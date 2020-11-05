package pl.cyfronet.s4e.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.cyfronet.s4e.properties.AmqpProperties;
import pl.cyfronet.s4e.service.SceneService;
import pl.cyfronet.s4e.sync.NotificationDispatcher;
import pl.cyfronet.s4e.sync.QueueReceiver;
import pl.cyfronet.s4e.sync.SceneAcceptor;

@Configuration
@ConditionalOnProperty(prefix = "amqp", name = "enabled")
@EnableRabbit
public class AmqpConfig {
    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private AmqpProperties amqpProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SceneAcceptor sceneAcceptor;

    @Autowired
    private SceneService sceneService;

    @Bean
    public String incomingQueueName() {
        return amqpProperties.getQueues().getIncoming();
    }

    @Bean
    public MessageConverter messageConverter() {
        val converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setUseProjectionForInterfaces(true);
        return converter;
    }

    @Bean
    public QueueCreator queueCreator() {
        return new QueueCreator(amqpAdmin, amqpProperties);
    }

    @Bean
    public NotificationDispatcher notificationDispatcher() {
        return new NotificationDispatcher(sceneAcceptor, sceneService);
    }

    @Bean
    public QueueReceiver queueReceiver() {
        return new QueueReceiver(notificationDispatcher());
    }
}
