package pl.cyfronet.s4e.listener;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.cyfronet.s4e.MailProperties;
import pl.cyfronet.s4e.bean.EmailVerification;
import pl.cyfronet.s4e.event.OnEmailConfirmedEvent;
import pl.cyfronet.s4e.event.OnRegistrationCompleteEvent;
import pl.cyfronet.s4e.event.OnResendRegistrationTokenEvent;
import pl.cyfronet.s4e.ex.NotFoundException;
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
    private final MailProperties mailProperties;

    @Async
    @EventListener
    public void handle(OnRegistrationCompleteEvent event) throws NotFoundException {
        sendConfirmationEmail(event.getRequesterEmail(), event.getLocale());
    }

    @Async
    @EventListener
    public void handle(OnResendRegistrationTokenEvent event) throws NotFoundException {
        String requesterEmail = event.getRequesterEmail();

        Optional<EmailVerification> optionalExistingVerificationToken = emailVerificationService.findByAppUserEmail(requesterEmail);
        if (optionalExistingVerificationToken.isPresent()) {
            emailVerificationService.delete(optionalExistingVerificationToken.get().getId());
        }

        sendConfirmationEmail(requesterEmail, event.getLocale());
    }

    @Async
    @EventListener
    public void handle(OnEmailConfirmedEvent event) {
        String email = event.getRequesterEmail();

        emailVerificationService.delete(event.getEmailVerificationId());

        String recipientAddress = email;
        String subject = messageSource.getMessage("email.account-activated.subject", null, event.getLocale());

        Context ctx = new Context(event.getLocale());
        ctx.setVariable("email", email);

        String plainText = templateEngine.process("account-activated.txt", ctx);
        String htmlText = templateEngine.process("account-activated.html", ctx);

        mailService.sendEmail(recipientAddress, subject, plainText, htmlText);
    }

    private void sendConfirmationEmail(String email, Locale locale) throws NotFoundException {
        val verificationToken = emailVerificationService.create(email);

        String recipientAddress = email;
        String subject = messageSource.getMessage("email.confirm-email.subject", null, locale);
        String activationUrl = mailProperties.getUrlDomain() + "/activate/" + verificationToken.getToken();

        Context ctx = new Context(locale);
        ctx.setVariable("email", email);
        ctx.setVariable("activationUrl", activationUrl);

        String plainText = templateEngine.process("confirm-email.txt", ctx);
        String htmlText = templateEngine.process("confirm-email.html", ctx);

        mailService.sendEmail(recipientAddress, subject, plainText, htmlText);
    }
}
