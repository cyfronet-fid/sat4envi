package pl.cyfronet.s4e.listener;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.EmailVerification;
import pl.cyfronet.s4e.event.OnEmailConfirmedEvent;
import pl.cyfronet.s4e.event.OnRegistrationCompleteEvent;
import pl.cyfronet.s4e.event.OnResendRegistrationTokenEvent;
import pl.cyfronet.s4e.service.EmailVerificationService;

import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmailVerificationListener {
    private final JavaMailSender javaMailSender;
    private final EmailVerificationService emailVerificationService;
    private final MessageSource messageSource;

    @Async
    @EventListener
    public void handle(OnRegistrationCompleteEvent event) {
        sendConfirmationEmail(event.getAppUser(), event.getLocale());
    }

    @Async
    @EventListener
    public void handle(OnResendRegistrationTokenEvent event) {
        AppUser appUser = event.getAppUser();

        Optional<EmailVerification> optionalExistingVerificationToken = emailVerificationService.findByAppUserId(appUser.getId());
        if (optionalExistingVerificationToken.isPresent()) {
            emailVerificationService.delete(optionalExistingVerificationToken.get().getId());
        }

        sendConfirmationEmail(appUser, event.getLocale());
    }

    @Async
    @EventListener
    public void handle(OnEmailConfirmedEvent event) {
        EmailVerification emailVerification = event.getEmailVerification();
        AppUser appUser = emailVerification.getAppUser();

        emailVerificationService.delete(emailVerification.getId());

        String recipientAddress = appUser.getEmail();
        String subject = messageSource.getMessage("email.account-activated.subject", null, event.getLocale());

        sendEmail(
                recipientAddress,
                subject,
                messageSource.getMessage("email.account-activated.text", new Object[]{
                        appUser.getEmail()
                }, event.getLocale()));
    }

    private void sendConfirmationEmail(AppUser appUser, Locale locale) {
        val verificationToken = emailVerificationService.create(appUser);

        String recipientAddress = appUser.getEmail();
        String subject = messageSource.getMessage("email.confirm-email.subject", null, locale);
        String confirmationUrl = "/confirm-email?token=" + verificationToken.getToken();

        String text = messageSource.getMessage("email.confirm-email.text", new Object[]{confirmationUrl}, locale);

        sendEmail(recipientAddress, subject, text);
    }

    private void sendEmail(String to, String subject, String text) {
        val email = new SimpleMailMessage();
        email.setTo(to);
        email.setSubject(subject);
        email.setText(text);
        javaMailSender.send(email);
    }
}
