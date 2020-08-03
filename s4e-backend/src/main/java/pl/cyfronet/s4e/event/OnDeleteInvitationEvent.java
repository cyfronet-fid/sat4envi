package pl.cyfronet.s4e.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

@Getter
public class OnDeleteInvitationEvent extends ApplicationEvent {
    private final String token;
    private final Locale locale;

    public OnDeleteInvitationEvent(String token, Locale locale) {
        super(token);

        this.token = token;
        this.locale = locale;
    }
}
