package pl.cyfronet.s4e;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.mail.util.MimeMessageParser;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

@Profile("development")
@Component
@Slf4j
public class LoggingMailSender extends JavaMailSenderImpl {
    @Override
    protected void doSend(MimeMessage[] mimeMessages, Object[] originalMessages) throws MailException {
        for (val mimeMessage: mimeMessages) {
            log.info(messageToString(mimeMessage));
        }
    }

    private static String messageToString(MimeMessage mimeMessage) {
        try {
            MimeMessageParser parser = new MimeMessageParser(mimeMessage).parse();
            StringBuilder sb = new StringBuilder();
            sb.append("\nTO: ").append(parser.getTo());
            sb.append("\nSUBJECT: ").append(parser.getSubject());
            if (parser.hasPlainContent()) {
                sb.append("\nPLAIN TEXT:\n").append(parser.getPlainContent());
            }
            if (parser.hasHtmlContent()) {
                sb.append("\nHTML TEXT:\n").append(parser.getHtmlContent());
            }
            return sb.toString();
        } catch (Exception e) {
            throw new MailParseException(e);
        }
    }

    @Override
    public void testConnection() { }
}
