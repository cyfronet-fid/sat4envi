package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.Group;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.controller.request.CreateChildInstitutionRequest;
import pl.cyfronet.s4e.controller.request.CreateInstitutionRequest;
import pl.cyfronet.s4e.controller.request.UpdateInstitutionRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.GroupRepository;
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
    public static final String DEFAULT = "default";
    public static final String PREFIX = "__";
    public static final String SUFFIX = "__";
    private final InstitutionRepository institutionRepository;
    private final AppUserRepository appUserRepository;
    private final GroupRepository groupRepository;
    private final UserRoleService userRoleService;
    private final SlugService slugService;
    private final FileStorageProperties fileStorageProperties;
    private final FileStorage fileStorage;

    @Transactional(rollbackFor = InstitutionCreationException.class)
    public void save(CreateInstitutionRequest request) throws InstitutionCreationException, NotFoundException {
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
    }

    @Transactional(rollbackFor = InstitutionCreationException.class)
    public Institution save(Institution institution) throws InstitutionCreationException {
        try {
            Institution result = institutionRepository.save(institution);
            Group group = Group.builder().institution(result).name(PREFIX + DEFAULT + SUFFIX).slug(DEFAULT).build();
            groupRepository.save(group);
            return result;
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
                .parent(getInstitution(institutionSlug, Institution.class)
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
        Set<String> adminEmails = groupRepository.findAllMembersEmails(institutionSlug, DEFAULT, AppRole.INST_ADMIN);
        for (String adminEmail : adminEmails) {
            addInstitutionAdminIfRequested(adminEmail, leafInstitutionSlug);
        }
    }

    public void addInstitutionAdminIfRequested(String adminMail, String institutionSlug) throws NotFoundException {
        if (adminMail != null) {
            val appUser = appUserRepository.findByEmail(adminMail);
            if (appUser.isPresent()) {
                // add member to default group
                userRoleService.addRole(AppRole.GROUP_MEMBER, adminMail, institutionSlug, "default");
                // add institution_admin role for user
                userRoleService.addRole(AppRole.INST_ADMIN, adminMail, institutionSlug, "default");
            } else {
                // TODO: invite
            }
        }
    }

    public <T> Optional<T> getInstitution(String slug, Class<T> projection) {
        return institutionRepository.findBySlug(slug, projection);
    }

    public <T> List<T> getAll(Class<T> projection) {
        return institutionRepository.findAllBy(projection);
    }

    @Transactional(rollbackFor = {InstitutionUpdateException.class, NotFoundException.class})
    public void update(UpdateInstitutionRequest request, String institutionSlug)
            throws NotFoundException, S3ClientException {
        val institution = getInstitution(institutionSlug, Institution.class)
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
        val institution = getInstitution(childSlug, Institution.class)
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
        val institution = getInstitution(childSlug, Institution.class)
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
