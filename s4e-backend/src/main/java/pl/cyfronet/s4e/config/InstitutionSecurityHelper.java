package pl.cyfronet.s4e.config;

import lombok.val;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.service.InstitutionService;
import pl.cyfronet.s4e.util.AppUserDetailsSupplier;

public class InstitutionSecurityHelper {
    private static final String DEFAULT_GROUP_SLUG = InstitutionService.DEFAULT;

    public boolean isAdmin(String institutionSlug) {
        AppUserDetails details = AppUserDetailsSupplier.get();
        if (details == null) {
            return false;
        }
        val authorities = details.getAuthorities();
        return authorities.contains(simpleGrantedAuthority(institutionSlug, AppRole.INST_ADMIN))
                ||
                authorities.contains(simpleGrantedAuthority(institutionSlug, AppRole.INST_MANAGER));
    }

    public boolean isMember(String institutionSlug) {
        AppUserDetails details = AppUserDetailsSupplier.get();
        if (details == null) {
            return false;
        }
        val authorities = details.getAuthorities();
        return authorities.contains(simpleGrantedAuthority(institutionSlug, AppRole.GROUP_MEMBER));
    }

    private SimpleGrantedAuthority simpleGrantedAuthority(String institutionSlug, AppRole appRole) {
        String role = String.join("_", "ROLE", appRole.name(), institutionSlug, DEFAULT_GROUP_SLUG);
        return new SimpleGrantedAuthority(role);
    }
}
