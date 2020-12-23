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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.cyfronet.s4e.bean.EmailVerification;
import pl.cyfronet.s4e.event.OnEmailConfirmedEvent;
import pl.cyfronet.s4e.event.OnRegistrationCompleteEvent;
import pl.cyfronet.s4e.event.OnRegistrationDuplicateEvent;
import pl.cyfronet.s4e.event.OnResendRegistrationTokenEvent;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.EmailVerificationService;
import pl.cyfronet.s4e.service.MailService;
import pl.cyfronet.s4e.util.MailHelper;

import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RegistrationListener {
    private final EmailVerificationService emailVerificationService;
    private final MessageSource messageSource;
    private final TemplateEngine templateEngine;
    private final MailService mailService;
    private final MailHelper mailHelper;

    @Async
    @EventListener
    public void handle(OnRegistrationCompleteEvent event) throws NotFoundException {
        sendConfirmationEmail(event.getRequesterEmail(), event.getLocale());
    }

    @Async
    @EventListener
    public void handle(OnRegistrationDuplicateEvent event) throws NotFoundException {
        sendRegistrationDuplicateEmail(event.getRequesterEmail(), event.getLocale());
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

        String subject = messageSource.getMessage("email.account-activated.subject", null, event.getLocale());

        Context ctx = new Context(event.getLocale());
        mailHelper.injectCommonVariables(ctx);
        ctx.setVariable("email", email);

        String plainText = templateEngine.process("account-activated.txt", ctx);
        String htmlText = templateEngine.process("account-activated.html", ctx);

        mailService.sendEmail(email, subject, plainText, htmlText);
    }

    private void sendConfirmationEmail(String email, Locale locale) throws NotFoundException {
        val verificationToken = emailVerificationService.create(email);

        String recipientAddress = email;
        String subject = messageSource.getMessage("email.confirm-email.subject", null, locale);
        String activationUrl = mailHelper.prefixWithDomain("/activate/" + verificationToken.getToken());

        Context ctx = new Context(locale);
        mailHelper.injectCommonVariables(ctx);
        ctx.setVariable("email", email);
        ctx.setVariable("activationUrl", activationUrl);

        String plainText = templateEngine.process("confirm-email.txt", ctx);
        String htmlText = templateEngine.process("confirm-email.html", ctx);

        mailService.sendEmail(recipientAddress, subject, plainText, htmlText);
    }

    private void sendRegistrationDuplicateEmail(String recipientAddress, Locale locale) throws NotFoundException {
        String subject = messageSource.getMessage("email.duplicate-registration.subject", null, locale);

        Context ctx = new Context(locale);
        mailHelper.injectCommonVariables(ctx);
        ctx.setVariable("email", recipientAddress);

        String plainText = templateEngine.process("duplicate-registration.txt", ctx);
        String htmlText = templateEngine.process("duplicate-registration.html", ctx);

        mailService.sendEmail(recipientAddress, subject, plainText, htmlText);
    }
}
