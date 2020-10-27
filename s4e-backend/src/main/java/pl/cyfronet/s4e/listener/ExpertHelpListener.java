package pl.cyfronet.s4e.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.cyfronet.s4e.event.OnSendHelpRequestEvent;
import pl.cyfronet.s4e.service.MailService;
import pl.cyfronet.s4e.util.MailHelper;

@Component
@RequiredArgsConstructor
public class ExpertHelpListener {
    private final MessageSource messageSource;
    private final TemplateEngine templateEngine;
    private final MailService mailService;
    private final MailHelper mailHelper;

    @Async
    @EventListener
    @Transactional(readOnly = true)
    public void handle(OnSendHelpRequestEvent event) {
        sendEmailToExpertBy(event);
        sendConfirmationEmailToUserBy(event);
    }

    private void sendConfirmationEmailToUserBy(OnSendHelpRequestEvent event) {
        String subject = messageSource.getMessage(
                "email.expert-help-sending-confirmation.subject",
                null,
                event.getLocale()
        );

        Context ctx = new Context(event.getLocale());
        mailHelper.injectCommonVariables(ctx);
        ctx.setVariable("helpType", event.getHelpType());
        ctx.setVariable("issueDescription", event.getIssueDescription());

        String plainText = templateEngine.process("expert-help-sending-confirmation.txt", ctx);
        String htmlText = templateEngine.process("expert-help-sending-confirmation.html", ctx);

        mailService.sendEmail(event.getRequestingUserEmail(), subject, plainText, htmlText);
    }

    private void sendEmailToExpertBy(OnSendHelpRequestEvent event) {
        Object[] requestingUserEmailSubject = new Object[]{event.getRequestingUserEmail()};
        String subject = messageSource.getMessage(
                "email.expert-help.subject",
                requestingUserEmailSubject,
                event.getLocale()
        );

        Context ctx = new Context(event.getLocale());
        mailHelper.injectCommonVariables(ctx);
        ctx.setVariable("requestingUserEmail", event.getRequestingUserEmail());
        ctx.setVariable("helpType", event.getHelpType());
        ctx.setVariable("issueDescription", event.getIssueDescription());

        String plainText = templateEngine.process("expert-help.txt", ctx);
        String htmlText = templateEngine.process("expert-help.html", ctx);

        mailService.sendEmail(helper -> {
            helper.setTo(event.getExpertEmail());
            helper.setReplyTo(event.getRequestingUserEmail());
            helper.setSubject(subject);
            helper.setText(plainText, htmlText);
        });
    }
}
