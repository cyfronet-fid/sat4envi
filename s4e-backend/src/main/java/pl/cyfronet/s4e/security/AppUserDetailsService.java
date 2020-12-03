package pl.cyfronet.s4e.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
import java.util.stream.Collectors;

import static pl.cyfronet.s4e.security.SecurityConstants.LICENSE_READ_AUTHORITY_PREFIX;
import static pl.cyfronet.s4e.security.SecurityConstants.LICENSE_WRITE_AUTHORITY_PREFIX;

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
                appUser.getId(),
                appUser.getEmail(),
                appUser.getName(),
                appUser.getSurname(),
                getAuthorities(appUser),
                appUser.getPassword(),
                appUser.isEnabled());
    }

    private Set<SimpleGrantedAuthority> getAuthorities(AppUser appUser) {
        val sourceAuthorities = new HashSet<>(appUser.getAuthorities());

        appUser.getRoles().stream()
                .map(this::toRole)
                .forEach(sourceAuthorities::add);

        appUser.getRoles().stream()
                .map(UserRole::getInstitution)
                .map(Institution::getLicenseGrants)
                .flatMap(Collection::stream)
                .map(LicenseGrant::getProduct)
                .mapToLong(Product::getId)
                .distinct()
                .mapToObj(id -> LICENSE_READ_AUTHORITY_PREFIX + id)
                .forEach(sourceAuthorities::add);

        appUser.getRoles().stream()
                .filter(userRole -> userRole.getRole() == AppRole.INST_ADMIN)
                .map(UserRole::getInstitution)
                .map(Institution::getLicenseGrants)
                .flatMap(Collection::stream)
                .filter(LicenseGrant::isOwner)
                .map(LicenseGrant::getProduct)
                .mapToLong(Product::getId)
                .distinct()
                .mapToObj(id -> LICENSE_WRITE_AUTHORITY_PREFIX + id)
                .forEach(sourceAuthorities::add);

        boolean grantEumetsatLicense = appUser.getRoles().stream()
                .map(UserRole::getInstitution)
                .anyMatch(Institution::isEumetsatLicense);

        if (grantEumetsatLicense) {
            sourceAuthorities.add("LICENSE_EUMETSAT");
        }

        if (appUser.getRoles().stream().map(UserRole::getInstitution).anyMatch(Institution::isZk)) {
            sourceAuthorities.add("ROLE_MEMBER_ZK");
        }

        if (appUser.getRoles().stream().map(UserRole::getInstitution).anyMatch(Institution::isPak)) {
            sourceAuthorities.add("ROLE_MEMBER_PAK");
        }

        return sourceAuthorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    private String toRole(UserRole userRole) {
        return "ROLE_" + userRole.getRole().name() + "_" + userRole.getInstitution().getSlug();
    }
}
