package pl.cyfronet.s4e;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Profile("development")
@Component
@Slf4j
public class LoggingMailSender extends JavaMailSenderImpl {
    @Override
    protected void doSend(MimeMessage[] mimeMessages, Object[] originalMessages) throws MailException {
        for (val mimeMessage: mimeMessages) {
            try {
                log.info(messageToString(mimeMessage));
            } catch (MessagingException | IOException e) {
                throw new MailParseException(e);
            }
        }
    }

    protected static String messageToString(MimeMessage mimeMessage) throws MessagingException, IOException {
        return "\n" +
                "To: "+getRecipientsString(mimeMessage)+"\n"+
                "Subject: "+mimeMessage.getSubject()+"\n"+
                mimeMessage.getContent();
    }

    private static String getRecipientsString(MimeMessage mimeMessage) throws MessagingException {
            Address[] recipients = mimeMessage.getRecipients(Message.RecipientType.TO);
            List<String> addresses = Arrays.stream(recipients)
                    .map(rec -> (InternetAddress) rec)
                    .map(addr -> addr.getAddress())
                    .collect(Collectors.toList());
            return String.join(",", addresses);
    }
}
