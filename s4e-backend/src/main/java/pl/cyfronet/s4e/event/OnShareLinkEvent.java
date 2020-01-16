package pl.cyfronet.s4e.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.Locale;

@Getter
public class OnShareLinkEvent extends ApplicationEvent {
    @Value
    @Builder
    public static class Request {
        String caption;
        String description;
        byte[] thumbnail;
        String path;
        List<String> emails;
    }

    private final String requesterEmail;
    private final Request request;
    private final Locale locale;

    public OnShareLinkEvent(String requesterEmail, Request request, Locale locale) {
        super(requesterEmail);

        this.requesterEmail = requesterEmail;
        this.request = request;
        this.locale = locale;
    }
}
