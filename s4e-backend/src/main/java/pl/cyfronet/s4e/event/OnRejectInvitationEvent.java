package pl.cyfronet.s4e.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.bean.Invitation;

import java.util.Locale;

@Getter
public class OnRejectInvitationEvent extends ApplicationEvent {
    private final String token;
    private final Locale locale;

    public OnRejectInvitationEvent(String token, Locale locale) {
        super(token);

        this.token = token;
        this.locale = locale;
    }
}
