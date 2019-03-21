package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.security.AppUserDetails;

import java.util.NoSuchElementException;

@Service("userDetailsService")
@RequiredArgsConstructor
@Slf4j
public class AppUserDetailsService implements UserDetailsService {
    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final AppUser appUser;
        try {
            appUser = appUserRepository.findByEmail(username).get();
        } catch (NoSuchElementException e) {
            log.debug("AppUser with email "+username+" not found", e);
            throw new UsernameNotFoundException("AppUser with email "+username+" not found", e);
        }

        return new AppUserDetails(appUser);
    }
}
