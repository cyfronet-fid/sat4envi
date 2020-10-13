package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.EmailVerification;
import pl.cyfronet.s4e.controller.request.RegisterRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.ex.AppUserDuplicateException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.RegistrationTokenExpiredException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppUserService {
    private final AppUserRepository appUserRepository;
    private final AppUserMapper appUserMapper;
    private final EmailVerificationService emailVerificationService;

    @Transactional(rollbackFor = AppUserDuplicateException.class)
    public AppUser register(RegisterRequest registerRequest) throws AppUserDuplicateException {
        val email = registerRequest.getEmail();
        if (appUserRepository.existsByEmail(email)) {
            throw new AppUserDuplicateException("AppUser already exists for email '" + email + "'");
        }

        val appUser = appUserMapper.requestToPreEntity(registerRequest);

        return appUserRepository.save(appUser);
    }

    @Transactional(rollbackFor = {NotFoundException.class, RegistrationTokenExpiredException.class})
    public EmailVerification confirmEmail(String token) throws NotFoundException, RegistrationTokenExpiredException {
        val verificationToken = emailVerificationService.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Provided token '" + token + "' not found"));

        if (verificationToken.getExpiryTimestamp().isBefore(LocalDateTime.now())) {
            throw new RegistrationTokenExpiredException("Provided token '" + token + "' expired");
        }

        val appUser = verificationToken.getAppUser();
        appUser.setEnabled(true);
        return verificationToken;
    }

    public String getRequesterEmailBy(String token) throws NotFoundException {
        val optionalToken = emailVerificationService.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Provided token '" + token + "' not found"));
        return optionalToken.getAppUser().getEmail();
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

    public <T> Optional<T> findByEmailWithRolesAndGroupsAndInstitution(String email, Class<T> projection) {
        return appUserRepository.findByEmailWithRolesAndInstitution(email, projection);
    }
}
