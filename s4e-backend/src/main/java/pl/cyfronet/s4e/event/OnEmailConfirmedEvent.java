package pl.cyfronet.s4e.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

@Getter
public class OnEmailConfirmedEvent extends ApplicationEvent {
    private final String requesterEmail;
    private final Long emailVerificationId;
    private final Locale locale;

    public OnEmailConfirmedEvent(String requesterEmail, Long emailVerificationId, Locale locale) {
        super(requesterEmail);

        this.requesterEmail = requesterEmail;
        this.emailVerificationId = emailVerificationId;
        this.locale = locale;
    }
}
