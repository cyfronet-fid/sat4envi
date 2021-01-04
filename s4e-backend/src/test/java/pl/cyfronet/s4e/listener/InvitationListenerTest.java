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

package pl.cyfronet.s4e.listener;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.thymeleaf.TemplateEngine;
import pl.cyfronet.s4e.InvitationTestHelper;
import pl.cyfronet.s4e.bean.Invitation;
import pl.cyfronet.s4e.event.OnConfirmInvitationEvent;
import pl.cyfronet.s4e.event.OnDeleteInvitationEvent;
import pl.cyfronet.s4e.event.OnRejectInvitationEvent;
import pl.cyfronet.s4e.event.OnSendInvitationEvent;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.InvitationService;
import pl.cyfronet.s4e.service.MailService;
import pl.cyfronet.s4e.util.MailHelper;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvitationListenerTest {
    @Mock
    private InvitationService invitationService;

    @Mock
    private MailService mailService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MailHelper mailHelper;

    @InjectMocks
    private InvitationListener listener;

    @Test
    public void shouldSendEmailOnCreate() throws NotFoundException {
        val invitation = InvitationTestHelper
                .invitationBuilder(InvitationTestHelper.institutionBuilder().build())
                .build();
        when(invitationService.findByToken(invitation.getToken(), Invitation.class))
                .thenReturn(Optional.of(invitation));

        listener.handle(new OnSendInvitationEvent(invitation.getToken(), null));

        verify(mailService).sendEmail(eq(invitation.getEmail()), any(), any(), any());
    }

    @Test
    public void shouldSendEmailOnConfirmAndRemoveInvitation() throws NotFoundException {
        val invitation = InvitationTestHelper
                .invitationBuilder(InvitationTestHelper.institutionBuilder().build())
                .build();

        when(invitationService.findByToken(invitation.getToken(), Invitation.class))
                .thenReturn(Optional.of(invitation));

        listener.handle(new OnConfirmInvitationEvent(invitation.getToken(), null));

        verify(mailService).sendEmail(eq(invitation.getEmail()), any(), any(), any());
        verify(invitationService).deleteByToken(eq(invitation.getToken()));
    }

    @Test
    public void shouldSendEmailOnRejection() throws NotFoundException {
        val invitation = InvitationTestHelper
                .invitationBuilder(InvitationTestHelper.institutionBuilder().build())
                .build();

        when(invitationService.findByToken(invitation.getToken(), Invitation.class))
                .thenReturn(Optional.of(invitation));

        listener.handle(new OnRejectInvitationEvent(invitation.getToken(), null));

        verify(mailService).sendEmail(eq(invitation.getEmail()), any(), any(), any());
    }

    @Test
    public void shouldSendEmailOnDeletionAndRemoveInvitation() throws NotFoundException {
        val invitation = InvitationTestHelper
                .invitationBuilder(InvitationTestHelper.institutionBuilder().build())
                .build();
        when(invitationService.findByToken(invitation.getToken(), Invitation.class))
                .thenReturn(Optional.of(invitation));

        listener.handle(new OnDeleteInvitationEvent(invitation.getToken(), null));

        verify(mailService).sendEmail(eq(invitation.getEmail()), any(), any(), any());
        verify(invitationService).deleteByToken(eq(invitation.getToken()));
    }
}
