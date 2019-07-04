package pl.cyfronet.s4e;

import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class LoggingMailSenderTest {
    @Test
    void shouldCallDoSend() {
        LoggingMailSender mailSender = spy(new LoggingMailSender());

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo("test@somewhere.pl");
        email.setSubject("Lorem ipsum sid dolor");
        email.setText("Amet and something else");
        mailSender.send(email);

        verify(mailSender).doSend(any(), any());
    }
}
