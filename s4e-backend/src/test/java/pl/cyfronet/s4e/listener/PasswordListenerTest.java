package pl.cyfronet.s4e.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.thymeleaf.TemplateEngine;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.PasswordReset;
import pl.cyfronet.s4e.event.OnPasswordResetTokenEmailEvent;
import pl.cyfronet.s4e.service.MailService;
import pl.cyfronet.s4e.service.PasswordService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PasswordListenerTest {
    private PasswordListener listener;
    private PasswordService passwordService;
    private MessageSource messageSource;
    private TemplateEngine templateEngine;
    private MailService mailService;

    @BeforeEach
    public void beforeEach() {
        passwordService = mock(PasswordService.class);
        messageSource = mock(MessageSource.class);
        templateEngine = mock(TemplateEngine.class);
        mailService = mock(MailService.class);
        listener = new PasswordListener(passwordService, messageSource, templateEngine, mailService);
    }

    @Test
    public void onPasswordResetTokenEmailEventShouldSendEmail() throws Exception {
        AppUser appUser = AppUser.builder()
                .email("some@email.pl")
                .name("Name")
                .surname("Surname")
                .build();
        PasswordReset passwordReset = PasswordReset.builder()
                .id(42L)
                .appUser(appUser)
                .token("someToken")
                .build();

        when(passwordService.createPasswordResetTokenForUser(appUser.getEmail())).thenReturn(passwordReset);

        listener.handle(new OnPasswordResetTokenEmailEvent(appUser.getEmail(), null));

        verify(mailService).sendEmail(eq(appUser.getEmail()), any(), any(), any());
    }
}
