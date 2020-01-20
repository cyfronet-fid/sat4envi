package pl.cyfronet.s4e.listener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.thymeleaf.TemplateEngine;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.PasswordReset;
import pl.cyfronet.s4e.event.OnPasswordResetTokenEmailEvent;
import pl.cyfronet.s4e.service.MailService;
import pl.cyfronet.s4e.service.PasswordService;
import pl.cyfronet.s4e.util.MailHelper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordListenerTest {
    @Mock
    private PasswordService passwordService;
    @Mock
    private MessageSource messageSource;
    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private MailService mailService;
    @Mock
    private MailHelper mailHelper;

    @InjectMocks
    private PasswordListener listener;

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
