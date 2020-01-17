package pl.cyfronet.s4e.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.cyfronet.s4e.bean.PasswordReset;
import pl.cyfronet.s4e.event.OnPasswordResetTokenEmailEvent;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.MailService;
import pl.cyfronet.s4e.service.PasswordService;
import pl.cyfronet.s4e.util.MailHelper;

@Component
@RequiredArgsConstructor
public class PasswordListener {
    private final PasswordService passwordService;
    private final MessageSource messageSource;
    private final TemplateEngine templateEngine;
    private final MailService mailService;
    private final MailHelper mailHelper;

    @Async
    @EventListener
    public void handle(OnPasswordResetTokenEmailEvent event) throws NotFoundException {
        PasswordReset reset = passwordService.createPasswordResetTokenForUser(event.getRequesterEmail());

        String recipientAddress = event.getRequesterEmail();
        String subject = messageSource.getMessage("email.password-reset.subject", null, event.getLocale());
        String resetPasswordUrl = mailHelper.prefixWithDomain("/password-reset/" + reset.getToken());

        Context ctx = new Context(event.getLocale());
        mailHelper.injectCommonVariables(ctx);
        ctx.setVariable("resetPasswordUrl", resetPasswordUrl);

        String plainText = templateEngine.process("password-reset.txt", ctx);
        String htmlText = templateEngine.process("password-reset.html", ctx);

        mailService.sendEmail(recipientAddress, subject, plainText, htmlText);
    }
}
