package pl.cyfronet.s4e.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

@Getter
public class OnRemoveFromGroupEvent extends ApplicationEvent {
    private final String removedMemberEmail;
    private final String groupSlug;
    private final String institutionSlug;
    private final Locale locale;

    public OnRemoveFromGroupEvent(String removedMemberEmail, String groupSlug, String institutionSlug, Locale locale) {
        super(removedMemberEmail);

        this.removedMemberEmail = removedMemberEmail;
        this.groupSlug = groupSlug;
        this.institutionSlug = institutionSlug;
        this.locale = locale;
    }
}
