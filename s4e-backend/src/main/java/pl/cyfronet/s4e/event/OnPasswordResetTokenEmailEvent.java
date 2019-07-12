package pl.cyfronet.s4e.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import pl.cyfronet.s4e.bean.AppUser;

import java.util.Locale;

@Getter
public class OnPasswordResetTokenEmailEvent extends ApplicationEvent {
    private final AppUser appUser;
    private final Locale locale;

    public OnPasswordResetTokenEmailEvent(AppUser appUser, Locale locale) {
        super(appUser);

        this.appUser = appUser;
        this.locale = locale;
    }
}
