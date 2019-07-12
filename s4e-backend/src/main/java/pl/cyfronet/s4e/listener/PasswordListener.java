package pl.cyfronet.s4e.listener;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.PasswordReset;
import pl.cyfronet.s4e.event.OnPasswordResetTokenEmailEvent;
import pl.cyfronet.s4e.service.PasswordService;

@Component
@RequiredArgsConstructor
public class PasswordListener {
    private final JavaMailSender javaMailSender;
    private final PasswordService passwordService;
    private final MessageSource messageSource;

    @Async
    @EventListener
    public void handle(OnPasswordResetTokenEmailEvent event) {
        AppUser appUser = event.getAppUser();
        PasswordReset reset = passwordService.createPasswordResetTokenForUser(appUser);

        String recipientAddress = appUser.getEmail();
        String subject = messageSource.getMessage("email.password-reset.subject", null, event.getLocale());
        String resetUrl = "/password-reset?token=" + reset.getToken();

        String text = messageSource.getMessage("email.password-reset.text", new Object[]{resetUrl}, event.getLocale());

        sendEmail(recipientAddress, subject, text);
    }

    private void sendEmail(String to, String subject, String text) {
        val email = new SimpleMailMessage();
        email.setTo(to);
        email.setSubject(subject);
        email.setText(text);
        javaMailSender.send(email);
    }
}
