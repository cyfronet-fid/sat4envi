package pl.cyfronet.s4e.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.data.repository.AppUserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service("userDetailsService")
@RequiredArgsConstructor
@Slf4j
public class AppUserDetailsService implements UserDetailsService {
    public static final String ROLE_PREFIX = "ROLE_";
    private final AppUserRepository appUserRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final AppUser appUser;
        try {
            appUser = appUserRepository.findByEmailWithRolesAndGroupsAndInstitution(username).get();
        } catch (NoSuchElementException e) {
            log.debug("AppUser with email " + username + " not found", e);
            throw new UsernameNotFoundException("AppUser with email " + username + " not found", e);
        }

        return new AppUserDetails(
                appUser.getEmail(),
                appUser.getName(),
                appUser.getSurname(),
                appUser.getRoles().stream().map(
                        userRole -> new SimpleGrantedAuthority(
                                ROLE_PREFIX
                                        + userRole.getRole().name()
                                        + "_"
                                        + userRole.getGroup().getInstitution().getSlug()
                                        + "_"
                                        + userRole.getGroup().getSlug()))
                        .collect(Collectors.toSet()),
                appUser.getPassword(),
                appUser.isEnabled(),
                appUser.isMemberZK(),
                appUser.isAdmin());
    }

}
