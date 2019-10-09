package pl.cyfronet.s4e.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Group;

import java.util.Locale;

@Getter
public class OnRemoveFromGroupEvent extends ApplicationEvent {
    private final AppUser appUser;
    private final Group group;
    private final Locale locale;

    public OnRemoveFromGroupEvent(AppUser appUser, Group group, Locale locale) {
        super(group);

        this.appUser = appUser;
        this.group = group;
        this.locale = locale;
    }
}
