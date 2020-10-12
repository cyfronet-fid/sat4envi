package pl.cyfronet.s4e.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.data.repository.AppUserRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import static pl.cyfronet.s4e.security.SecurityConstants.LICENSE_READ_AUTHORITY_PREFIX;

@Service("userDetailsService")
@RequiredArgsConstructor
@Slf4j
public class AppUserDetailsService implements UserDetailsService {
    private final AppUserRepository appUserRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final AppUser appUser;
        try {
            appUser = appUserRepository.findByEmailWithAllUpToLicensedProducts(username).get();
        } catch (NoSuchElementException e) {
            log.debug("AppUser with email " + username + " not found", e);
            throw new UsernameNotFoundException("AppUser with email " + username + " not found", e);
        }

        return new AppUserDetails(
                appUser.getEmail(),
                appUser.getName(),
                appUser.getSurname(),
                getAuthorities(appUser),
                appUser.getPassword(),
                appUser.isEnabled());
    }

    private Set<SimpleGrantedAuthority> getAuthorities(AppUser appUser) {
        Set<SimpleGrantedAuthority> roles = new HashSet<>();

        appUser.getRoles().stream()
                .map(this::toSimpleGrantedAuthority)
                .forEach(roles::add);

        appUser.getRoles().stream()
                .map(UserRole::getInstitution)
                .map(Institution::getLicenseGrants)
                .flatMap(Collection::stream)
                .map(LicenseGrant::getProduct)
                .mapToLong(Product::getId)
                .distinct()
                .mapToObj(id -> LICENSE_READ_AUTHORITY_PREFIX + id)
                .map(SimpleGrantedAuthority::new)
                .forEach(roles::add);

        boolean grantEumetsatLicense = appUser.isEumetsatLicense() || appUser.getRoles().stream()
                        .map(UserRole::getInstitution)
                        .anyMatch(Institution::isEumetsatLicense);

        if (grantEumetsatLicense) {
            roles.add(new SimpleGrantedAuthority("LICENSE_EUMETSAT"));
        }

        if (appUser.isAdmin()) {
            roles.add(new SimpleGrantedAuthority(toRole("ADMIN")));
        }

        if (appUser.isMemberZK()) {
            roles.add(new SimpleGrantedAuthority(toRole("MEMBER_ZK")));
        }

        return roles;
    }

    private SimpleGrantedAuthority toSimpleGrantedAuthority(UserRole userRole) {
        String role = toRole(
                userRole.getRole().name(),
                userRole.getInstitution().getSlug()
        );
        return new SimpleGrantedAuthority(role);
    }

    private String toRole(String... segments) {
        String[] segmentsWithPrefix = ArrayUtils.addFirst(segments, "ROLE");
        return String.join("_", segmentsWithPrefix);
    }
}
