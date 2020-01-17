package pl.cyfronet.s4e.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.thymeleaf.TemplateEngine;
import pl.cyfronet.s4e.MailProperties;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.EmailVerification;
import pl.cyfronet.s4e.event.OnResendRegistrationTokenEvent;
import pl.cyfronet.s4e.service.EmailVerificationService;
import pl.cyfronet.s4e.service.MailService;

import java.util.Optional;

import static org.mockito.Mockito.*;

class EmailVerificationListenerTest {
    private EmailVerificationListener listener;
    private EmailVerificationService emailVerificationService;
    private MessageSource messageSource;
    private TemplateEngine templateEngine;
    private MailService mailService;
    private MailProperties mailProperties;

    @BeforeEach
    public void beforeEach() {
        emailVerificationService = mock(EmailVerificationService.class);
        messageSource = mock(MessageSource.class);
        templateEngine = mock(TemplateEngine.class);
        mailService = mock(MailService.class);
        mailProperties = mock(MailProperties.class);
        listener = new EmailVerificationListener(emailVerificationService, messageSource, templateEngine, mailService, mailProperties);
    }

    @Test
    public void onResendRegistrationTokenEventShouldDeleteExistingToken() throws Exception {
        AppUser appUser = AppUser.builder()
                .email("some@email.pl")
                .name("Name")
                .surname("Surname")
                .build();
        EmailVerification emailVerification = EmailVerification.builder()
                .id(42L)
                .appUser(appUser)
                .token("someToken")
                .build();

        when(emailVerificationService.findByAppUserEmail(appUser.getEmail())).thenReturn(Optional.of(emailVerification));
        when(emailVerificationService.create(appUser.getEmail())).thenReturn(emailVerification);

        listener.handle(new OnResendRegistrationTokenEvent(appUser.getEmail(), null));

        verify(emailVerificationService).delete(42L);
    }

    @Test
    public void onResendRegistrationTokenEventShouldSendEmail() throws Exception {
        AppUser appUser = AppUser.builder()
                .email("some@email.pl")
                .name("Name")
                .surname("Surname")
                .build();
        EmailVerification emailVerification = EmailVerification.builder()
                .id(42L)
                .appUser(appUser)
                .token("someToken")
                .build();

        when(emailVerificationService.create(appUser.getEmail())).thenReturn(emailVerification);

        listener.handle(new OnResendRegistrationTokenEvent(appUser.getEmail(), null));

        verify(mailService).sendEmail(eq(appUser.getEmail()), any(), any(), any());
    }

}
