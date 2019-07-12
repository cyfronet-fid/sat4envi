package pl.cyfronet.s4e.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.PasswordReset;
import pl.cyfronet.s4e.event.OnPasswordResetTokenEmailEvent;
import pl.cyfronet.s4e.service.PasswordService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PasswordListenerTest {
    private PasswordListener listener;
    private JavaMailSender javaMailSender;
    private PasswordService passwordService;
    private MessageSource messageSource;

    @BeforeEach
    public void beforeEach() {
        javaMailSender = mock(JavaMailSender.class);
        passwordService = mock(PasswordService.class);
        messageSource = mock(MessageSource.class);
        listener = new PasswordListener(javaMailSender, passwordService, messageSource);
    }

    @Test
    public void onPasswordResetTokenEmailEventShouldSendEmail() {
        AppUser appUser = AppUser.builder()
                .email("some@email.pl")
                .build();
        PasswordReset passwordReset = PasswordReset.builder()
                .id(42L)
                .appUser(appUser)
                .token("someToken")
                .build();

        when(passwordService.createPasswordResetTokenForUser(appUser)).thenReturn(passwordReset);

        listener.handle(new OnPasswordResetTokenEmailEvent(appUser, null));

        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }
}
