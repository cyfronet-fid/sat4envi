package pl.cyfronet.s4e.listener;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import pl.cyfronet.s4e.controller.request.ExpertHelpRequest;
import pl.cyfronet.s4e.controller.request.HelpType;
import pl.cyfronet.s4e.event.OnSendHelpRequestEvent;
import pl.cyfronet.s4e.service.MailService;
import pl.cyfronet.s4e.util.MailHelper;

import javax.mail.MessagingException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    private MimeMessageHelper mimeMessageHelper;

    @InjectMocks
    private ExpertHelpListener listener;

    @Test
    public void shouldSendEmailOnCreate() throws MessagingException {
        val requestingUserEmail = "test@mail.pl";
        val expertEmail = "expert@mail.pl";
        val request = ExpertHelpRequest.builder()
                .helpType(HelpType.REMOTE)
                .issueDescription("Test issue")
                .build();
        val event = new OnSendHelpRequestEvent(
                requestingUserEmail,
                expertEmail,
                request.getHelpType(),
                request.getIssueDescription(),
                null
        );

        Mockito.doAnswer(invocationOnMock -> {
            MailService.Modifier modifier = invocationOnMock.getArgument(0);
            modifier.modify(mimeMessageHelper);
            return null;
        }).when(mailService).sendEmail(any());

        listener.handle(event);

        verify(mailService).sendEmail(
                eq(requestingUserEmail),
                any(),
                any(),
                any()
        );
        verify(mailService).sendEmail(any());
        verifyNoMoreInteractions(mailService);

        verify(mimeMessageHelper).setTo(expertEmail);
        verify(mimeMessageHelper).setReplyTo(requestingUserEmail);
        verify(mimeMessageHelper).setSubject(any());
        verify(mimeMessageHelper).setText(any(), any());
        verifyNoMoreInteractions(mimeMessageHelper);
    }
}
