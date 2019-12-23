package pl.cyfronet.s4e.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import pl.cyfronet.s4e.bean.AppUser;

import java.util.List;
import java.util.Locale;

@Getter
public class OnShareLinkEvent extends ApplicationEvent {
    private final AppUser user;
    private final String link;
    private final List<String> emails;
    private final Locale locale;

    public OnShareLinkEvent(AppUser appUser, String link, List<String> emails, Locale locale) {
        super(appUser);

        this.user = appUser;
        this.link = link;
        this.emails = emails;
        this.locale = locale;
    }
}
