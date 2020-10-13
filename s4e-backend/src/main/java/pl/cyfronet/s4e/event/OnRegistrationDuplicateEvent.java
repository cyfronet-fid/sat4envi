package pl.cyfronet.s4e.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

@Getter
public class OnRegistrationDuplicateEvent extends ApplicationEvent {
    private final String requesterEmail;
    private final Locale locale;

    public OnRegistrationDuplicateEvent(String requesterEmail, Locale locale) {
        super(requesterEmail);

        this.requesterEmail = requesterEmail;
        this.locale = locale;
    }
}
