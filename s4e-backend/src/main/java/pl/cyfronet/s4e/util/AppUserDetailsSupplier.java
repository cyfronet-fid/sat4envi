package pl.cyfronet.s4e.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.cyfronet.s4e.security.AppUserDetails;

import java.util.Optional;

public class AppUserDetailsSupplier {
    public static AppUserDetails get() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getDetails)
                .filter(AppUserDetails.class::isInstance)
                .map(AppUserDetails.class::cast)
                .orElse(null);
    }
}
