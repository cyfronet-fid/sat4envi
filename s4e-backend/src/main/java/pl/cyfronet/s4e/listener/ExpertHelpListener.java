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
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.cyfronet.s4e.controller.request.HelpType;
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
        String helpType = getHelpType(event);
        String subject = messageSource.getMessage(
                "email.expert-help-sending-confirmation.subject",
                null,
                event.getLocale()
        );

        Context ctx = new Context(event.getLocale());
        mailHelper.injectCommonVariables(ctx);
        ctx.setVariable("helpType", helpType);
        ctx.setVariable("issueDescription", event.getIssueDescription());

        String plainText = templateEngine.process("expert-help-sending-confirmation.txt", ctx);
        String htmlText = templateEngine.process("expert-help-sending-confirmation.html", ctx);

        mailService.sendEmail(event.getRequestingUserEmail(), subject, plainText, htmlText);
    }

    private void sendEmailToExpertBy(OnSendHelpRequestEvent event) {
        Object[] requestingUserEmailSubject = new Object[]{event.getRequestingUserEmail()};
        String helpType = getHelpType(event);
        String subject = messageSource.getMessage(
                "email.expert-help.subject",
                requestingUserEmailSubject,
                event.getLocale()
        );

        Context ctx = new Context(event.getLocale());
        mailHelper.injectCommonVariables(ctx);
        ctx.setVariable("requestingUserEmail", event.getRequestingUserEmail());
        ctx.setVariable("helpType", helpType);
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

    private String getHelpType(OnSendHelpRequestEvent event) {
        if (HelpType.REMOTE.equals(event.getHelpType())) {
            return messageSource.getMessage(
                    "email.expert-help.remote",
                    null,
                    event.getLocale()
            );
        }
        return messageSource.getMessage(
                "email.expert-help.at-location",
                null,
                event.getLocale()
        );
    }
}
