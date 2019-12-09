package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.controller.request.CreateUserWithGroupsRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.ex.AppUserCreationException;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppUserService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final GroupService groupService;

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
    public AppUser createFromRequest(CreateUserWithGroupsRequest request, String institutionSlug) throws AppUserCreationException, NotFoundException {
        AppUser appUser = AppUser.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .email(request.getEmail())
                // TODO: FIXME
                .password(passwordEncoder.encode("passTemporary"))
                .enabled(false)
                .build();

        save(appUser);
        if (request.getGroupSlugs() != null) {
            for (String groupSlug : request.getGroupSlugs()) {
                groupService.addMember(institutionSlug, groupSlug, appUser.getEmail());
            }
        }
        return appUser;
    }

    @Transactional
    public AppUser update(AppUser appUser) {
        return appUserRepository.save(appUser);
    }

    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }

    public Optional<AppUser> findByEmailWithRolesAndGroupsAndInstitution(String email) {
        return appUserRepository.findByEmailWithRolesAndGroupsAndInstitution(email);
    }
}
