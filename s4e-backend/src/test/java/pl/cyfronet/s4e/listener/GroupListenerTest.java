package pl.cyfronet.s4e.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.thymeleaf.TemplateEngine;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.event.OnShareLinkEvent;
import pl.cyfronet.s4e.service.MailService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GroupListenerTest {
    private GroupListener listener;
    private MessageSource messageSource;
    private TemplateEngine templateEngine;
    private MailService mailService;

    @BeforeEach
    public void beforeEach() {
        messageSource = mock(MessageSource.class);
        templateEngine = mock(TemplateEngine.class);
        mailService = mock(MailService.class);
        listener = new GroupListener(messageSource, templateEngine, mailService);
    }

    @Test
    public void onShareLinkEventShouldSendEmail() {
        AppUser appUser = AppUser.builder()
                .email("some@email.pl")
                .name("Name")
                .surname("Surname")
                .build();

        listener.handle(new OnShareLinkEvent(appUser, "this/is/link", List.of(), null));
        verify(mailService, times(0)).sendEmail(any(), any(), any());

        listener.handle(new OnShareLinkEvent(appUser, "this/is/link", List.of("some@email.pl"), null));
        verify(mailService, times(1)).sendEmail(any(), any(), any());

        listener.handle(new OnShareLinkEvent(appUser, "this/is/link", List.of("some@email.pl", "some2@email.pl"), null));
        verify(mailService, times(3)).sendEmail(any(), any(), any());
    }
}
