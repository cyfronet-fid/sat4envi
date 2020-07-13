package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.EmailVerification;
import pl.cyfronet.s4e.controller.request.CreateUserWithGroupsRequest;
import pl.cyfronet.s4e.controller.request.RegisterRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.ex.AppUserCreationException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.RegistrationTokenExpiredException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppUserService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleService userRoleService;
    private final EmailVerificationService emailVerificationService;

    @Transactional(rollbackFor = AppUserCreationException.class)
    public AppUser save(AppUser appUser) throws AppUserCreationException {
        try {
            return appUserRepository.save(appUser);
        } catch (DataIntegrityViolationException e) {
            log.info("Cannot create AppUser with email '" + appUser.getEmail() + "'", e);
            throw new AppUserCreationException(e);
        }
    }

    @Transactional(rollbackFor = AppUserCreationException.class)
    public AppUser register(RegisterRequest registerRequest) throws AppUserCreationException {
        return save(AppUser.builder()
                .name(registerRequest.getName())
                .surname(registerRequest.getSurname())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .enabled(false)
                .build());
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

    public <T> Optional<T> findByEmailWithRolesAndGroupsAndInstitution(String email, Class<T> projection) {
        return appUserRepository.findByEmailWithRolesAndGroupsAndInstitution(email, projection);
    }
}
