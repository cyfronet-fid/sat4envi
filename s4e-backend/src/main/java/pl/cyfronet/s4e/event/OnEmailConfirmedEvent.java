package pl.cyfronet.s4e.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import pl.cyfronet.s4e.bean.EmailVerification;

import java.util.Locale;

@Getter
public class OnEmailConfirmedEvent extends ApplicationEvent {
    private final EmailVerification emailVerification;
    private final Locale locale;

    public OnEmailConfirmedEvent(EmailVerification emailVerification, Locale locale) {
        super(emailVerification);

        this.emailVerification = emailVerification;
        this.locale = locale;
    }
}
