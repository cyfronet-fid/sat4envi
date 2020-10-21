package pl.cyfronet.s4e.listener;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.thymeleaf.TemplateEngine;
import pl.cyfronet.s4e.controller.request.ExpertHelpRequest;
import pl.cyfronet.s4e.controller.request.HelpType;
import pl.cyfronet.s4e.event.OnSendHelpRequestEvent;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.properties.ExpertHelpProperties;
import pl.cyfronet.s4e.service.MailService;
import pl.cyfronet.s4e.util.MailHelper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ExpertHelpListenerTest {

    @Mock
    private MailService mailService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MailHelper mailHelper;

    @Mock
    private ExpertHelpProperties expertHelpProperties;

    @InjectMocks
    private ExpertHelpListener listener;

    @Test
    public void shouldSendEmailOnCreate() throws NotFoundException {
        val requestingUserEmail = "test@mail.pl";
        val request = ExpertHelpRequest.builder()
                .helpType(HelpType.REMOTE)
                .issueDescription("Test issue")
                .build();
        val event = new OnSendHelpRequestEvent(
                requestingUserEmail,
                request.getHelpType(),
                request.getIssueDescription(),
                null
        );
        listener.handle(event);

        verify(mailService).sendEmail(eq(requestingUserEmail), any(), any(), any());
        verify(mailService).sendEmail(eq(expertHelpProperties.getMail()), any(), any(), any());
    }
}
