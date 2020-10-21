package pl.cyfronet.s4e.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import pl.cyfronet.s4e.controller.request.HelpType;

import java.util.Locale;

@Getter
public class OnSendHelpRequestEvent extends ApplicationEvent {
    private final String requestingUserEmail;
    private final HelpType helpType;
    private final String issueDescription;
    private final Locale locale;

    public OnSendHelpRequestEvent(String requestingUserEmail, HelpType helpType, String issueDescription, Locale locale) {
        super(requestingUserEmail);

        this.requestingUserEmail = requestingUserEmail;
        this.helpType = helpType;
        this.issueDescription = issueDescription;
        this.locale = locale;
    }
}
