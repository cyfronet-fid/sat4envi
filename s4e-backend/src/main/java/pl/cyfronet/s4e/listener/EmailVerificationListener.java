package pl.cyfronet.s4e.listener;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.EmailVerification;
import pl.cyfronet.s4e.event.OnEmailConfirmedEvent;
import pl.cyfronet.s4e.event.OnRegistrationCompleteEvent;
import pl.cyfronet.s4e.event.OnResendRegistrationTokenEvent;
import pl.cyfronet.s4e.service.EmailVerificationService;
import pl.cyfronet.s4e.service.MailService;

import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmailVerificationListener {
    private final EmailVerificationService emailVerificationService;
    private final MessageSource messageSource;
    private final TemplateEngine templateEngine;
    private final MailService mailService;

    @Value("${mail.urlDomain}")
    private String urlDomain;

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

        Context ctx = new Context(event.getLocale());
        ctx.setVariable("email", appUser.getEmail());

        String content = templateEngine.process("account-activated.txt", ctx);

        mailService.sendEmail(recipientAddress, subject, content);
    }

    private void sendConfirmationEmail(AppUser appUser, Locale locale) {
        val verificationToken = emailVerificationService.create(appUser);

        String recipientAddress = appUser.getEmail();
        String subject = messageSource.getMessage("email.confirm-email.subject", null, locale);
        String activationUrl = urlDomain + "/activate/" + verificationToken.getToken();

        Context ctx = new Context(locale);
        ctx.setVariable("email", appUser.getEmail());
        ctx.setVariable("activationUrl", activationUrl);

        String content = templateEngine.process("confirm-email.txt", ctx);

        mailService.sendEmail(recipientAddress, subject, content);
    }
}
