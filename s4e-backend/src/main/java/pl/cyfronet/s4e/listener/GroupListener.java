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
import pl.cyfronet.s4e.MailProperties;
import pl.cyfronet.s4e.bean.Group;
import pl.cyfronet.s4e.event.OnAddToGroupEvent;
import pl.cyfronet.s4e.event.OnRemoveFromGroupEvent;
import pl.cyfronet.s4e.event.OnShareLinkEvent;
import pl.cyfronet.s4e.service.GroupService;
import pl.cyfronet.s4e.service.MailService;

import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;

@Component
@RequiredArgsConstructor
public class GroupListener {
    private final MessageSource messageSource;
    private final TemplateEngine templateEngine;
    private final MailService mailService;
    private final GroupService groupService;
    private final MailProperties mailProperties;

    @Async
    @EventListener
    @Transactional(readOnly = true)
    public void handle(OnAddToGroupEvent event) {
        Group group = groupService.getGroup(event.getInstitutionSlug(), event.getGroupSlug(), Group.class).get();

        String recipientAddress = event.getAddedMemberEmail();
        String subject = messageSource.getMessage("email.group-add.subject", null, event.getLocale());

        Context ctx = new Context(event.getLocale());
        ctx.setVariable("groupName", group.getName());
        ctx.setVariable("institutionName", group.getInstitution().getName());

        String plainText = templateEngine.process("group-add-member.txt", ctx);
        String htmlText = templateEngine.process("group-add-member.html", ctx);

        mailService.sendEmail(recipientAddress, subject, plainText, htmlText);
    }

    @Async
    @EventListener
    @Transactional(readOnly = true)
    public void handle(OnRemoveFromGroupEvent event) {
        Group group = groupService.getGroup(event.getInstitutionSlug(), event.getGroupSlug(), Group.class).get();

        String recipientAddress = event.getRemovedMemberEmail();
        String subject = messageSource.getMessage("email.group-remove.subject", null, event.getLocale());

        Context ctx = new Context(event.getLocale());
        ctx.setVariable("groupName", group.getName());
        ctx.setVariable("institutionName", group.getInstitution().getName());

        String plainText = templateEngine.process("group-remove-member.txt", ctx);
        String htmlText = templateEngine.process("group-remove-member.html", ctx);

        mailService.sendEmail(recipientAddress, subject, plainText, htmlText);
    }

    @Async
    @EventListener
    public void handle(OnShareLinkEvent event) throws IOException {
        String subject = messageSource.getMessage("email.share-link.subject", null, event.getLocale());
        Context ctx = new Context(event.getLocale());

        val req = event.getRequest();
        ctx.setVariable("email", event.getRequesterEmail());
        ctx.setVariable("caption", req.getCaption());
        ctx.setVariable("description", req.getDescription());
        ctx.setVariable("url", mailProperties.getUrlDomain() + req.getPath());

        String plainText = templateEngine.process("share-link.txt", ctx);
        String htmlText = templateEngine.process("share-link.html", ctx);

        String thumbnailContentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(req.getThumbnail()));

        for (String recipientAddress : req.getEmails()) {
            mailService.sendEmail(helper -> {
                helper.setTo(recipientAddress);
                helper.setSubject(subject);
                helper.setText(plainText, htmlText);
                helper.addAttachment("thumbnail", new ByteArrayDataSource(req.getThumbnail(), thumbnailContentType));
            });
        }
    }
}
