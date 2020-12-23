/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.controller.request.ForgetUserRequest;
import pl.cyfronet.s4e.controller.request.RegisterRequest;
import pl.cyfronet.s4e.data.repository.*;
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
    private final InvitationRepository invitationRepository;
    private final WMSOverlayRepository wmsOverlayRepository;
    private final UserRoleRepository userRoleRepository;
    private final ReportTemplateRepository reportTemplateRepository;
    private final AppUserMapper appUserMapper;
    private final PasswordEncoder passwordEncoder;
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

    public boolean matches(String password, String hashPassword) {
        return passwordEncoder.matches(password, hashPassword);
    }

    @Transactional
    public void forgetUser(ForgetUserRequest forgetUserRequest) throws NotFoundException {
        val appUser = appUserRepository.findByEmail(forgetUserRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("User: '" + forgetUserRequest.getEmail() + "' not found"));

        removeUserWmsOverlays(appUser);
        removeUserReports(appUser);
        removeUserInvitations(appUser);
        removeUserRoles(appUser);

        anonymiseUser(appUser);
    }

    private void removeUserWmsOverlays(AppUser appUser) {
        for (WMSOverlay wmsOverlay : wmsOverlayRepository.findAllPersonal(appUser.getId(), OverlayOwner.PERSONAL)) {
            wmsOverlayRepository.delete(wmsOverlay);
        }
    }

    private void removeUserReports(AppUser appUser) {
        for (ReportTemplate reportTemplate :
                reportTemplateRepository.findAllByOwnerEmail(
                        appUser.getEmail(),
                        Sort.by("createdAt"),
                        ReportTemplate.class)) {
            reportTemplateRepository.delete(reportTemplate);
        }
    }

    private void removeUserInvitations(AppUser appUser) {
        for (Invitation invitation : invitationRepository.findAllByEmail(appUser.getEmail(), Invitation.class)) {
            invitationRepository.delete(invitation);
        }
    }

    private void removeUserRoles(AppUser appUser) {
        for (UserRole userRole : userRoleRepository.findByUser_Id(appUser.getId())) {
            userRole.getInstitution().removeMemberRole(userRole);
            userRole.getUser().removeRole(userRole);
        }
    }

    private void anonymiseUser(AppUser appUser) {
        appUser.setName(appUser.getId().toString());
        appUser.setSurname(appUser.getId().toString());
        appUser.setEmail(appUser.getId().toString());
    }
}
