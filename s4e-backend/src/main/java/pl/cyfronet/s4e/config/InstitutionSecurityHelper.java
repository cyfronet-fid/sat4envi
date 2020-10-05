package pl.cyfronet.s4e.config;

import lombok.val;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.util.AppUserDetailsSupplier;

public class InstitutionSecurityHelper {
    public boolean isAdmin(String institutionSlug) {
        AppUserDetails details = AppUserDetailsSupplier.get();
        if (details == null) {
            return false;
        }
        val authorities = details.getAuthorities();
        return authorities.contains(simpleGrantedAuthority(institutionSlug, AppRole.INST_ADMIN));
    }

    public boolean isMember(String institutionSlug) {
        AppUserDetails details = AppUserDetailsSupplier.get();
        if (details == null) {
            return false;
        }
        val authorities = details.getAuthorities();
        return authorities.contains(simpleGrantedAuthority(institutionSlug, AppRole.INST_MEMBER));
    }

    private SimpleGrantedAuthority simpleGrantedAuthority(String institutionSlug, AppRole appRole) {
        String role = String.join("_", "ROLE", appRole.name(), institutionSlug);
        return new SimpleGrantedAuthority(role);
    }
}
