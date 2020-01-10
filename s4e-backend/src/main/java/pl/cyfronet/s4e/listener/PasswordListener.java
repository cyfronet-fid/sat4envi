package pl.cyfronet.s4e.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.PasswordReset;
import pl.cyfronet.s4e.event.OnPasswordResetTokenEmailEvent;
import pl.cyfronet.s4e.service.MailService;
import pl.cyfronet.s4e.service.PasswordService;

@Component
@RequiredArgsConstructor
public class PasswordListener {
    private final PasswordService passwordService;
    private final MessageSource messageSource;
    private final TemplateEngine templateEngine;
    private final MailService mailService;

    @Value("${mail.urlDomain}")
    private String urlDomain;

    @Async
    @EventListener
    public void handle(OnPasswordResetTokenEmailEvent event) {
        AppUser appUser = event.getAppUser();
        PasswordReset reset = passwordService.createPasswordResetTokenForUser(appUser);

        String recipientAddress = appUser.getEmail();
        String subject = messageSource.getMessage("email.password-reset.subject", null, event.getLocale());
        String resetPasswordUrl = urlDomain + "/password-reset/" + reset.getToken();

        Context ctx = new Context(event.getLocale());
        ctx.setVariable("resetPasswordUrl", resetPasswordUrl);

        String plainText = templateEngine.process("password-reset.txt", ctx);
        String htmlText = templateEngine.process("password-reset.html", ctx);

        mailService.sendEmail(recipientAddress, subject, plainText, htmlText);
    }
}
