package pl.cyfronet.s4e.listener;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.cyfronet.s4e.bean.Invitation;
import pl.cyfronet.s4e.event.OnConfirmInvitationEvent;
import pl.cyfronet.s4e.event.OnDeleteInvitationEvent;
import pl.cyfronet.s4e.event.OnRejectInvitationEvent;
import pl.cyfronet.s4e.event.OnSendInvitationEvent;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.InvitationService;
import pl.cyfronet.s4e.service.MailService;
import pl.cyfronet.s4e.util.MailHelper;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class InvitationListener {
    private final InvitationService invitationService;

    private final MessageSource messageSource;
    private final TemplateEngine templateEngine;
    private final MailService mailService;
    private final MailHelper mailHelper;

    @Async
    @EventListener
    @Transactional
    public void handle(OnSendInvitationEvent event) throws NotFoundException {
        val invitation = invitationService.findByToken(event.getToken(), Invitation.class)
                .orElseThrow(() -> new NotFoundException("Invitation with your token doesn't exist"));
        this.send(invitation, event.getLocale());
    }

    @Async
    @EventListener
    @Transactional
    public void handle(OnConfirmInvitationEvent event) throws NotFoundException {
        val invitation = invitationService.findByToken(event.getToken(), Invitation.class)
                .orElseThrow(() -> new NotFoundException("Invitation with your token doesn't exist"));
        this.sendConfirmation(invitation, event.getLocale());
        this.invitationService.deleteByToken(event.getToken());
    }

    @Async
    @EventListener
    @Transactional
    public void handle(OnRejectInvitationEvent event) throws NotFoundException {
        val invitation = invitationService.findByToken(event.getToken(), Invitation.class)
                .orElseThrow(() -> new NotFoundException("Invitation with your token doesn't exist"));
        this.sendRejection(invitation, event.getLocale());
    }

    @Async
    @EventListener
    @Transactional
    public void handle(OnDeleteInvitationEvent event) throws NotFoundException {
        val invitation = invitationService.findByToken(event.getToken(), Invitation.class)
                .orElseThrow(() -> new NotFoundException("Invitation with your token doesn't exist"));
        this.sendDeletion(invitation, event.getLocale());
        this.invitationService.deleteByToken(event.getToken());
    }

    private void send(Invitation invitation, Locale locale) {
        Context ctx = new Context(locale);
        mailHelper.injectCommonVariables(ctx);

        ctx.setVariable("email", invitation.getEmail());
        ctx.setVariable("institutionName", invitation.getInstitution().getName());

        String confirmUrl = mailHelper.prefixWithDomain("/login?token=" + invitation.getToken());
        String rejectUrl = mailHelper.prefixWithDomain("/login?reject=true&token=" + invitation.getToken());
        ctx.setVariable("confirmUrl", confirmUrl);
        ctx.setVariable("rejectUrl", rejectUrl);

        String plainText = templateEngine.process("invitation-email.txt", ctx);
        String htmlText = templateEngine.process("invitation-email.html", ctx);

        Object[] subjectInstitution = new Object[]{invitation.getInstitution().getName()};
        String subject = messageSource.getMessage("email.invitation.subject", subjectInstitution, locale);
        mailService.sendEmail(invitation.getEmail(), subject, plainText, htmlText);
    }

    private void sendConfirmation(Invitation invitation, Locale locale) {
        Context ctx = new Context(locale);
        mailHelper.injectCommonVariables(ctx);

        ctx.setVariable("email", invitation.getEmail());
        ctx.setVariable("institutionName", invitation.getInstitution().getName());

        String plainText = templateEngine.process("confirm-invitation-email.txt", ctx);
        String htmlText = templateEngine.process("confirm-invitation-email.html", ctx);

        Object[] subjectInstitution = new Object[]{invitation.getInstitution().getName()};
        String subject = messageSource.getMessage("email.invitation-confirmation.subject", subjectInstitution, locale);
        mailService.sendEmail(invitation.getEmail(), subject, plainText, htmlText);
    }

    private void sendRejection(Invitation invitation, Locale locale) {
        Context ctx = new Context(locale);
        mailHelper.injectCommonVariables(ctx);

        ctx.setVariable("email", invitation.getEmail());
        ctx.setVariable("institutionName", invitation.getInstitution().getName());

        String plainText = templateEngine.process("reject-invitation-email.txt", ctx);
        String htmlText = templateEngine.process("reject-invitation-email.html", ctx);

        Object[] subjectInstitution = new Object[]{invitation.getInstitution().getName()};
        String subject = messageSource.getMessage("email.invitation-rejection.subject", subjectInstitution, locale);
        mailService.sendEmail(invitation.getEmail(), subject, plainText, htmlText);
    }

    private void sendDeletion(Invitation invitation, Locale locale) {
        Context ctx = new Context(locale);
        mailHelper.injectCommonVariables(ctx);

        ctx.setVariable("email", invitation.getEmail());
        ctx.setVariable("institutionName", invitation.getInstitution().getName());

        String plainText = templateEngine.process("delete-invitation-email.txt", ctx);
        String htmlText = templateEngine.process("delete-invitation-email.html", ctx);

        Object[] subjectInstitution = new Object[]{invitation.getInstitution().getName()};
        String subject = messageSource.getMessage("email.invitation-deletion.subject", subjectInstitution, locale);
        mailService.sendEmail(invitation.getEmail(), subject, plainText, htmlText);
    }
}
