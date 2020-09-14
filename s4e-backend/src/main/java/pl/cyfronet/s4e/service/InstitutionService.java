package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.controller.request.CreateChildInstitutionRequest;
import pl.cyfronet.s4e.controller.request.CreateInstitutionRequest;
import pl.cyfronet.s4e.controller.request.UpdateInstitutionRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.ex.InstitutionCreationException;
import pl.cyfronet.s4e.ex.InstitutionUpdateException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.S3ClientException;
import pl.cyfronet.s4e.properties.FileStorageProperties;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstitutionService {
    private final InstitutionRepository institutionRepository;
    private final AppUserRepository appUserRepository;
    private final UserRoleService userRoleService;
    private final SlugService slugService;
    private final FileStorageProperties fileStorageProperties;
    private final FileStorage fileStorage;

    @Transactional(rollbackFor = InstitutionCreationException.class)
    public String save(CreateInstitutionRequest request) throws InstitutionCreationException, NotFoundException {
        String slug = slugService.slugify(request.getName());
        save(Institution.builder()
                .name(request.getName())
                .slug(slug)
                .parent(null)
                .address(request.getAddress())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .phone(request.getPhone())
                .secondaryPhone(request.getSecondaryPhone())
                .build());
        addInstitutionAdminIfRequested(request.getInstitutionAdminEmail(), slug);
        uploadEmblemIfRequested(slug, request.getEmblem());

        return slug;
    }

    @Transactional(rollbackFor = InstitutionCreationException.class)
    public Institution save(Institution institution) throws InstitutionCreationException {
        try {
            return institutionRepository.save(institution);
        } catch (DataIntegrityViolationException e) {
            log.info("Cannot create Institution with name '" + institution.getName() + "'", e);
            throw new InstitutionCreationException("Cannot create Institution", e);
        }
    }

    @Transactional(rollbackFor = {InstitutionCreationException.class, NotFoundException.class})
    public Institution createChildInstitution(CreateChildInstitutionRequest request, String institutionSlug)
            throws InstitutionCreationException, NotFoundException {
        // create child institution with default group
        Institution result = save(Institution.builder()
                .name(request.getName())
                .slug(slugService.slugify(request.getName()))
                .parent(findBySlug(institutionSlug, Institution.class)
                        .orElseThrow(() -> new NotFoundException("Institution not found for id '" + institutionSlug + "'")))
                .address(request.getAddress())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .phone(request.getPhone())
                .secondaryPhone(request.getSecondaryPhone())
                .build());
        uploadEmblemIfRequested(slugService.slugify(request.getName()), request.getEmblem());
        addInstitutionAdminIfRequested(request.getInstitutionAdminEmail(), result.getSlug());
        // add all over-admins as members and admins
        addParentInstitutionAdministrators(institutionSlug, result.getSlug());
        return result;
    }

    private void addParentInstitutionAdministrators(String institutionSlug, String leafInstitutionSlug) throws NotFoundException {
        val institution = institutionRepository.findBySlug(institutionSlug, Institution.class)
                .orElseThrow(() -> new NotFoundException("Institution not found for id '" + institutionSlug + "'"));
        if (institution.getParent() != null) {
            Institution parent = institution.getParent();
            addParentInstitutionAdministrators(parent.getSlug(), leafInstitutionSlug);
        }
        // look for institution admins and add them to leaf institution
        Set<String> adminEmails = institutionRepository.findAllMembersEmails(institutionSlug, AppRole.INST_ADMIN);
        for (String adminEmail : adminEmails) {
            addInstitutionAdminIfRequested(adminEmail, leafInstitutionSlug);
        }
    }

    public void addInstitutionAdminIfRequested(String adminMail, String institutionSlug) throws NotFoundException {
        if (adminMail != null) {
            val appUser = appUserRepository.findByEmail(adminMail);
            if (appUser.isPresent()) {
                // add member to default group
                userRoleService.addRole(AppRole.GROUP_MEMBER, adminMail, institutionSlug);
                // add institution_admin role for user
                userRoleService.addRole(AppRole.INST_ADMIN, adminMail, institutionSlug);
            } else {
                // TODO: invite
            }
        }
    }

    public <T> Set<T> getMembers(String institutionSlug, Class<T> projection) {
        return institutionRepository.findAllMembers(institutionSlug, AppRole.GROUP_MEMBER, projection);
    }

    public <T> List<T> getAll(Class<T> projection) {
        return institutionRepository.findAllBy(projection);
    }

    public <T> Optional<T> findBySlug(String slug, Class<T> projection) {
        return institutionRepository.findBySlug(slug, projection);
    }

    public void addMember(String institutionSlug, String email) throws NotFoundException {
        userRoleService.addRole(AppRole.GROUP_MEMBER, email, institutionSlug);
    }

    public void removeMember(String institutionSlug, String email) throws NotFoundException {
        userRoleService.removeRole(AppRole.GROUP_MEMBER, email, institutionSlug);
    }

    @Transactional(rollbackFor = {InstitutionUpdateException.class, NotFoundException.class})
    public String update(UpdateInstitutionRequest request, String institutionSlug)
            throws NotFoundException, S3ClientException {
        val institution = findBySlug(institutionSlug, Institution.class)
                .orElseThrow(() -> new NotFoundException("Institution not found for id '" + institutionSlug));
        String slug = slugService.slugify(request.getName());
        if (fileStorage.exists(getEmblemKey(institutionSlug))) {
            fileStorage.delete(getEmblemKey(institutionSlug));
        }
        uploadEmblemIfRequested(slug, request.getEmblem());

        institution.setName(request.getName());
        institution.setSlug(slug);
        institution.setAddress(request.getAddress());
        institution.setCity(request.getCity());
        institution.setPostalCode(request.getPostalCode());
        institution.setPhone(request.getPhone());
        institution.setSecondaryPhone(request.getSecondaryPhone());

        return slug;
    }

    @Transactional
    public void delete(String slug) {
        institutionRepository.deleteInstitutionBySlug(slug);
    }

    public <T> List<T> getUserInstitutionsBy(String email, List<String> roles, Class<T> projection) {
        return institutionRepository.findInstitutionByUserEmailAndRoles(email, roles, projection);
    }

    @Transactional
    public String getParentSlugBy(String childSlug) throws NotFoundException {
        val institution = findBySlug(childSlug, Institution.class)
                .orElseThrow(() -> new NotFoundException("Institution not found for id '" + childSlug));
        if (institution.getParent() != null) {
            Institution parent = institution.getParent();
            return parent.getSlug();
        } else {
            return null;
        }
    }

    @Transactional
    public String getParentNameBy(String childSlug) throws NotFoundException {
        val institution = findBySlug(childSlug, Institution.class)
                .orElseThrow(() -> new NotFoundException("Institution not found for id '" + childSlug));
        if (institution.getParent() != null) {
            Institution parent = institution.getParent();
            return parent.getName();
        } else {
            return null;
        }
    }

    public String getEmblemPath(String slug) {
        return String.join("/", fileStorageProperties.getBucket(), getEmblemKey(slug));
    }

    public String getEmblemKey(String slug) {
        return fileStorageProperties.getKeyPrefixEmblem() + slug;
    }

    private void uploadEmblemIfRequested(String slug, String emblem) {
        //upload file to s3
        if (emblem != null) {
            fileStorage.upload(getEmblemKey(slug),
                    Base64.getDecoder().decode(emblem));
        }
    }
}
