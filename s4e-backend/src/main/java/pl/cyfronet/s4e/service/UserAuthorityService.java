package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.data.repository.AppUserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAuthorityService {
    private final AppUserRepository appUserRepository;

    public <T> List<T> findAllUsersByAuthority(String authority, Class<T> projection) {
        return appUserRepository.findAllByAuthority(authority, projection);
    }

    @Transactional
    public Optional<Boolean> addAuthority(String email, String authority) {
        return appUserRepository.findByEmail(email)
                .map(appUser -> appUser.addAuthority(authority));
    }

    @Transactional
    public Optional<Boolean> removeAuthority(String email, String authority) {
        return appUserRepository.findByEmail(email)
                .map(appUser -> appUser.removeAuthority(authority));
    }
}
