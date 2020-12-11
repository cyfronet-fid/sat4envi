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
            helper.setFrom(from);
            modifier.modify(helper);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            log.info("Sending email failed", e);
        }
    }
}
