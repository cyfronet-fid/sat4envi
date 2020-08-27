package pl.cyfronet.s4e.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

@Getter
public class OnAddToInstitutionEvent extends ApplicationEvent {
    private final String addedMemberEmail;
    private final String institutionSlug;
    private final Locale locale;

    public OnAddToInstitutionEvent(String addedMemberEmail, String institutionSlug, Locale locale) {
        super(addedMemberEmail);

        this.addedMemberEmail = addedMemberEmail;
        this.institutionSlug = institutionSlug;
        this.locale = locale;
    }
}
