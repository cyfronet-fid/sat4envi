/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.event.OnAddToInstitutionEvent;
import pl.cyfronet.s4e.event.OnRemoveFromInstitutionEvent;
import pl.cyfronet.s4e.event.OnShareLinkEvent;
import pl.cyfronet.s4e.service.InstitutionService;
import pl.cyfronet.s4e.service.MailService;
import pl.cyfronet.s4e.util.MailHelper;

import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;

@Component
@RequiredArgsConstructor
public class InstitutionListener {
    private final MessageSource messageSource;
    private final TemplateEngine templateEngine;
    private final MailService mailService;
    private final InstitutionService institutionService;
    private final MailHelper mailHelper;

    @Async
    @EventListener
    @Transactional(readOnly = true)
    public void handle(OnAddToInstitutionEvent event) {
        Institution institution = institutionService.findBySlug(event.getInstitutionSlug(), Institution.class).get();

        String recipientAddress = event.getAddedMemberEmail();
        String subject = messageSource.getMessage("email.institution-add.subject", null, event.getLocale());

        Context ctx = new Context(event.getLocale());
        mailHelper.injectCommonVariables(ctx);
        ctx.setVariable("institutionName", institution.getName());

        String plainText = templateEngine.process("institution-add-member.txt", ctx);
        String htmlText = templateEngine.process("institution-add-member.html", ctx);

        mailService.sendEmail(recipientAddress, subject, plainText, htmlText);
    }

    @Async
    @EventListener
    @Transactional(readOnly = true)
    public void handle(OnRemoveFromInstitutionEvent event) {
        Institution institution = institutionService.findBySlug(event.getInstitutionSlug(), Institution.class).get();

        String recipientAddress = event.getRemovedMemberEmail();
        String subject = messageSource.getMessage("email.institution-remove.subject", null, event.getLocale());

        Context ctx = new Context(event.getLocale());
        mailHelper.injectCommonVariables(ctx);
        ctx.setVariable("institutionName", institution.getName());

        String plainText = templateEngine.process("institution-remove-member.txt", ctx);
        String htmlText = templateEngine.process("institution-remove-member.html", ctx);

        mailService.sendEmail(recipientAddress, subject, plainText, htmlText);
    }

    @Async
    @EventListener
    public void handle(OnShareLinkEvent event) throws IOException {
        String subject = messageSource.getMessage("email.share-link.subject", null, event.getLocale());
        val req = event.getRequest();

        Context ctx = new Context(event.getLocale());
        mailHelper.injectCommonVariables(ctx);
        ctx.setVariable("email", event.getRequesterEmail());
        ctx.setVariable("caption", req.getCaption());
        ctx.setVariable("description", req.getDescription());
        ctx.setVariable("url", mailHelper.prefixWithDomain(req.getPath()));

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
