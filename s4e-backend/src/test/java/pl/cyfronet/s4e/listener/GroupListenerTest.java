package pl.cyfronet.s4e.listener;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.thymeleaf.TemplateEngine;
import pl.cyfronet.s4e.MailProperties;
import pl.cyfronet.s4e.event.OnShareLinkEvent;
import pl.cyfronet.s4e.service.GroupService;
import pl.cyfronet.s4e.service.MailService;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.*;

public class GroupListenerTest {
    private GroupListener listener;
    private MessageSource messageSource;
    private TemplateEngine templateEngine;
    private MailService mailService;
    private GroupService groupService;
    private MailProperties mailProperties;

    @BeforeEach
    public void beforeEach() {
        messageSource = mock(MessageSource.class);
        templateEngine = mock(TemplateEngine.class);
        mailService = mock(MailService.class);
        groupService = mock(GroupService.class);
        mailProperties = mock(MailProperties.class);
        listener = new GroupListener(messageSource, templateEngine, mailService, groupService, mailProperties);
    }

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
