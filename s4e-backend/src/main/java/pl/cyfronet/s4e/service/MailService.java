package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {
    public interface Modifier {
        void modify(MimeMessageHelper helper) throws MessagingException;
    }

    private final JavaMailSender javaMailSender;

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
            modifier.modify(helper);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            log.info("Sending email failed", e);
        }
    }
}
