package pl.cyfronet.s4e.security;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.service.InstitutionService;

public class CustomMethodSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {
    public CustomMethodSecurityExpressionRoot(Authentication authentication) {
        super(authentication);
    }

    @Override
    public void setFilterObject(Object o) {

    }

    @Override
    public Object getFilterObject() {
        return null;
    }

    @Override
    public void setReturnObject(Object o) {

    }

    @Override
    public Object getReturnObject() {
        return null;
    }

    @Override
    public Object getThis() {
        return null;
    }

    public boolean isInstitutionAdmin(String institutionSlug) {
        return
                hasRole(institutionSlug, InstitutionService.DEFAULT, AppRole.INST_ADMIN.name())
                        ||
                hasRole(institutionSlug, InstitutionService.DEFAULT, AppRole.INST_MANAGER.name());
    }

    public boolean isInstitutionMember(String institutionSlug) {
        return hasRole(institutionSlug, InstitutionService.DEFAULT, AppRole.GROUP_MEMBER.name());
    }

    private boolean hasRole(String institutionSlug, String groupSlug, String role) {
        AppUserDetails appUserDetails = (AppUserDetails) this.getPrincipal();
        for (SimpleGrantedAuthority auth : appUserDetails.getRoles()) {
            if (auth.getAuthority().contains("ROLE_" + role + "_" + institutionSlug + "_" + groupSlug)) {
                return true;
            }
        }
        return false;
    }

    public boolean isZKMember() {
        AppUserDetails appUserDetails = (AppUserDetails) this.getPrincipal();
        return appUserDetails.isMemberZK();
    }

    public boolean isAdmin() {
        AppUserDetails appUserDetails = (AppUserDetails) this.getPrincipal();
        return appUserDetails.isAdmin();
    }

    public boolean isOwner() {
        return false;
    }

    public boolean canRead(Object object) {
        return false;
    }

    public boolean canWrite(Object object) {
        return false;
    }
}
