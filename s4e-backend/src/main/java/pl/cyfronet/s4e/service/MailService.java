/*
 * Copyright 2021 ACC Cyfronet AGH
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

package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Optional;

@Service
@Slf4j
public class MailService {
    public interface Modifier {
        void modify(MimeMessageHelper helper) throws MessagingException;
    }

    public MailService(JavaMailSender javaMailSender, Optional<MailProperties> mailProperties) {
        this.javaMailSender = javaMailSender;
        this.from = mailProperties.map(MailProperties::getUsername).orElse(null);
    }

    private final JavaMailSender javaMailSender;

    private final String from;

    public void sendEmail(String to, String subject, String plainText, String htmlText) {
        sendEmail(helper -> {
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainText, htmlText);
        });
    }

    public void sendEmail(Modifier modifier) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            if (from != null) {
                helper.setFrom(from);
            }
            modifier.modify(helper);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            log.info("Sending email failed", e);
        }
    }
}
