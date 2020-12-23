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

package pl.cyfronet.s4e.listener;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.thymeleaf.TemplateEngine;
import pl.cyfronet.s4e.event.OnShareLinkEvent;
import pl.cyfronet.s4e.service.MailService;
import pl.cyfronet.s4e.util.MailHelper;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InstitutionListenerTest {
    @Mock
    private MessageSource messageSource;
    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private MailService mailService;
    @Mock
    private MailHelper mailHelper;

    @InjectMocks
    private InstitutionListener listener;

    @Test
    public void shouldntSendIfEmailsEmptyOnShareLinkEvent() throws IOException {
        val request = OnShareLinkEvent.Request.builder()
                .emails(List.of())
                .thumbnail("".getBytes())
                .build();
        listener.handle(new OnShareLinkEvent("some@email.pl", request, null));
        verifyNoInteractions(mailService);
    }

    @Test
    public void shouldSendMultipleEmailsOnShareLinkEvent() throws IOException {
        val request = OnShareLinkEvent.Request.builder()
                .emails(List.of("some@email.pl", "some2@email.pl"))
                .thumbnail("".getBytes())
                .build();
        listener.handle(new OnShareLinkEvent("some@email.pl", request, null));
        verify(mailService, times(2)).sendEmail(any());
    }
}
