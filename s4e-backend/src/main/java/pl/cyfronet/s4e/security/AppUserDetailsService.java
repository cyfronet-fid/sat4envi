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
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.UserRole;
import pl.cyfronet.s4e.data.repository.AppUserRepository;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

@Service("userDetailsService")
@RequiredArgsConstructor
@Slf4j
public class AppUserDetailsService implements UserDetailsService {
    private final AppUserRepository appUserRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final AppUser appUser;
        try {
            appUser = appUserRepository.findByEmailWithRolesAndGroupsAndInstitution(username, AppUser.class).get();
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
                .map(this::simpleGrantedAuthority)
                .forEach(roles::add);

        if (appUser.isAdmin()) {
            roles.add(new SimpleGrantedAuthority(toRole("ADMIN")));
        }

        if (appUser.isMemberZK()) {
            roles.add(new SimpleGrantedAuthority(toRole("MEMBER_ZK")));
        }

        return roles;
    }

    private SimpleGrantedAuthority simpleGrantedAuthority(UserRole userRole) {
        String role = toRole(
                userRole.getRole().name(),
                userRole.getGroup().getInstitution().getSlug(),
                userRole.getGroup().getSlug()
        );
        return new SimpleGrantedAuthority(role);
    }

    private String toRole(String... segments) {
        String[] segmentsWithPrefix = ArrayUtils.addFirst(segments, "ROLE");
        return String.join("_", segmentsWithPrefix);
    }

}
