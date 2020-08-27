package pl.cyfronet.s4e.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

@Getter
public class OnRemoveFromInstitutionEvent extends ApplicationEvent {
    private final String removedMemberEmail;
    private final String institutionSlug;
    private final Locale locale;

    public OnRemoveFromInstitutionEvent(String removedMemberEmail, String institutionSlug, Locale locale) {
        super(removedMemberEmail);

        this.removedMemberEmail = removedMemberEmail;
        this.institutionSlug = institutionSlug;
        this.locale = locale;
    }
}
