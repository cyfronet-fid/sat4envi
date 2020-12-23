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
