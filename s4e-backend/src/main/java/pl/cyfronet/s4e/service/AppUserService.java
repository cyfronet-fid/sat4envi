package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.ex.AppUserCreationException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppUserService {
    private final AppUserRepository appUserRepository;

    @Transactional(rollbackFor = AppUserCreationException.class)
    public AppUser save(AppUser appUser) throws AppUserCreationException {
        try {
            return appUserRepository.save(appUser);
        } catch (DataIntegrityViolationException e) {
            log.info("Cannot create AppUser with email '" + appUser.getEmail() + "'", e);
            throw new AppUserCreationException(e);
        }
    }

    @Transactional
    public AppUser update(AppUser appUser) {
        return appUserRepository.save(appUser);
    }

    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }

    public Optional<AppUser> findById(Long id) {
        return appUserRepository.findById(id);
    }
}
