package pl.cyfronet.s4e.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

@Getter
public class OnAddToGroupEvent extends ApplicationEvent {
    private final String addedMemberEmail;
    private final String groupSlug;
    private final String institutionSlug;
    private final Locale locale;

    public OnAddToGroupEvent(String addedMemberEmail, String groupSlug, String institutionSlug, Locale locale) {
        super(addedMemberEmail);

        this.addedMemberEmail = addedMemberEmail;
        this.groupSlug = groupSlug;
        this.institutionSlug = institutionSlug;
        this.locale = locale;
    }
}
