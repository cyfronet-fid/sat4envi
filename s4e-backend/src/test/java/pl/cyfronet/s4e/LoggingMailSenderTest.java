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

package pl.cyfronet.s4e;

import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class LoggingMailSenderTest {
    @Test
    void shouldCallDoSend() {
        LoggingMailSender mailSender = spy(new LoggingMailSender());

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo("test@somewhere.pl");
        email.setSubject("Lorem ipsum sid dolor");
        email.setText("Amet and something else");
        mailSender.send(email);

        verify(mailSender).doSend(any(), any());
    }
}
