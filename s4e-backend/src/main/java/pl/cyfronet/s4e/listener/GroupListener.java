package pl.cyfronet.s4e.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Group;
import pl.cyfronet.s4e.event.OnAddToGroupEvent;
import pl.cyfronet.s4e.event.OnRemoveFromGroupEvent;
import pl.cyfronet.s4e.service.MailService;

@Component
@RequiredArgsConstructor
public class GroupListener {
    private final MessageSource messageSource;
    private final TemplateEngine templateEngine;
    private final MailService mailService;

    @Async
    @EventListener
    public void handle(OnAddToGroupEvent event) {
        AppUser appUser = event.getAppUser();
        Group group = event.getGroup();

        String recipientAddress = appUser.getEmail();
        String subject = messageSource.getMessage("email.group-add.subject", null, event.getLocale());

        Context ctx = new Context(event.getLocale());
        ctx.setVariable("groupName", group.getName());
        ctx.setVariable("institutionName", group.getInstitution().getName());

        String content = templateEngine.process("group-add-member.txt", ctx);

        mailService.sendEmail(recipientAddress, subject, content);
    }

    @Async
    @EventListener
    public void handle(OnRemoveFromGroupEvent event) {
        AppUser appUser = event.getAppUser();
        Group group = event.getGroup();

        String recipientAddress = appUser.getEmail();
        String subject = messageSource.getMessage("email.group-remove.subject", null, event.getLocale());

        Context ctx = new Context(event.getLocale());
        ctx.setVariable("groupName", group.getName());
        ctx.setVariable("institutionName", group.getInstitution().getName());

        String content = templateEngine.process("group-remove-member.txt", ctx);

        mailService.sendEmail(recipientAddress, subject, content);
    }


}
