/*
 * Copyright 2020 ACC Cyfronet AGH
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
