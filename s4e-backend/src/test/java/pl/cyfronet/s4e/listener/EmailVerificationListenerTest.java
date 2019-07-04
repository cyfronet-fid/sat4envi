package pl.cyfronet.s4e.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.EmailVerification;
import pl.cyfronet.s4e.event.OnResendRegistrationTokenEvent;
import pl.cyfronet.s4e.service.EmailVerificationService;

import java.util.Optional;

import static org.mockito.Mockito.*;

class EmailVerificationListenerTest {
    private EmailVerificationListener listener;
    private JavaMailSender javaMailSender;
    private EmailVerificationService emailVerificationService;
    private MessageSource messageSource;

    @BeforeEach
    public void beforeEach() {
        javaMailSender = mock(JavaMailSender.class);
        emailVerificationService = mock(EmailVerificationService.class);
        messageSource = mock(MessageSource.class);
        listener = new EmailVerificationListener(javaMailSender, emailVerificationService, messageSource);
    }

    @Test
    public void onResendRegistrationTokenEventShouldDeleteExistingToken() {
        AppUser appUser = AppUser.builder()
                .email("some@email.pl")
                .build();
        EmailVerification emailVerification = EmailVerification.builder()
                .id(42L)
                .appUser(appUser)
                .token("someToken")
                .build();

        when(emailVerificationService.findByAppUserId(appUser.getId())).thenReturn(Optional.of(emailVerification));
        when(emailVerificationService.create(appUser)).thenReturn(emailVerification);

        listener.handle(new OnResendRegistrationTokenEvent(appUser, null));

        verify(emailVerificationService).delete(42L);
    }

    @Test
    public void onResendRegistrationTokenEventShouldSendEmail() {
        AppUser appUser = AppUser.builder()
                .email("some@email.pl")
                .build();
        EmailVerification emailVerification = EmailVerification.builder()
                .id(42L)
                .appUser(appUser)
                .token("someToken")
                .build();

        when(emailVerificationService.create(appUser)).thenReturn(emailVerification);

        listener.handle(new OnResendRegistrationTokenEvent(appUser, null));

        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

}
