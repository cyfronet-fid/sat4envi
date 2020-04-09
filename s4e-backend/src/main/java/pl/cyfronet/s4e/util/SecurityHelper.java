package pl.cyfronet.s4e.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.security.AppUserDetails;

@Service
public class SecurityHelper {
    public AppUserDetails getUserDetailsIfAvailable() {
        if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null) {
            return (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }
        return null;
    }
}
