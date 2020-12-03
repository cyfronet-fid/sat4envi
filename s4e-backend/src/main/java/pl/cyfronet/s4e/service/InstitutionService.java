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
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.ex.*;
import pl.cyfronet.s4e.properties.FileStorageProperties;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstitutionService {
    private final InstitutionRepository institutionRepository;
    private final UserRoleService userRoleService;
    private final FileStorageProperties fileStorageProperties;
    private final FileStorage fileStorage;
    private final InstitutionMapper institutionMapper;

    @Transactional(rollbackFor = InstitutionCreationException.class)
    public String create(CreateInstitutionRequest request) throws InstitutionCreationException {
        Institution newInstitution = doSave(institutionMapper.requestToPreEntity(request));

        uploadEmblemIfRequested(newInstitution.getSlug(), request.getEmblem());

        return newInstitution.getSlug();
    }

    @Transactional(rollbackFor = InstitutionCreationException.class)
    public String create(Institution institution) throws InstitutionCreationException {
        return doSave(institution).getSlug();
    }

    @Transactional(rollbackFor = {InstitutionCreationException.class, NotFoundException.class})
    public String createChild(CreateChildInstitutionRequest request, String parentInstitutionSlug)
            throws InstitutionCreationException, NotFoundException {
        Institution parentInstitution = findBySlug(parentInstitutionSlug, Institution.class)
                .orElseThrow(() -> new NotFoundException("Institution not found for id '" + parentInstitutionSlug + "'"));
        Institution newInstitution = doSave(institutionMapper.requestToPreEntity(request));
        newInstitution.setParent(parentInstitution);
        newInstitution.setZk(parentInstitution.isZk());
        newInstitution.setPak(parentInstitution.isPak());

        String newSlug = newInstitution.getSlug();

        uploadEmblemIfRequested(newSlug, request.getEmblem());

        for (val userRole : parentInstitution.getMembersRoles()) {
            if (userRole.getRole() == AppRole.INST_ADMIN) {
                // This should be propagated to the member role automatically.
                userRoleService.addRole(newSlug, userRole.getUser().getId(), AppRole.INST_ADMIN);
            }
        }

        return newSlug;
    }

    private Institution doSave(Institution institution) throws InstitutionCreationException {
        try {
            return institutionRepository.save(institution);
        } catch (DataIntegrityViolationException e) {
            log.info("Cannot create Institution with name '" + institution.getName() + "'", e);
            throw new InstitutionCreationException("Cannot create Institution", e);
        }
    }

    @Transactional
    public void setZk(String institutionSlug) throws NotFoundException, InstitutionAttributeException {
        setAttribute(institutionSlug, "ZK", Institution::setZk, Institution::isZk);
    }

    @Transactional
    public void unsetZk(String institutionSlug) throws NotFoundException, InstitutionAttributeException {
        unsetAttribute(institutionSlug, "ZK", Institution::setZk, Institution::isZk);
    }

    @Transactional
    public void setPak(String institutionSlug) throws NotFoundException, InstitutionAttributeException {
        setAttribute(institutionSlug, "PAK", Institution::setPak, Institution::isPak);
    }

    @Transactional
    public void unsetPak(String institutionSlug) throws NotFoundException, InstitutionAttributeException {
        unsetAttribute(institutionSlug, "PAK", Institution::setPak, Institution::isPak);
    }

    private void setAttribute(
            String institutionSlug,
            String name,
            BiConsumer<Institution, Boolean> set,
            Function<Institution, Boolean> get
    ) throws NotFoundException, InstitutionAttributeException {
        val institution = institutionRepository.findBySlug(institutionSlug)
                .orElseThrow(() -> new NotFoundException("Institution not found for id '" + institutionSlug + "'"));

        if (get.apply(institution)) {
            throw new InstitutionAttributeException("Institution '" + institutionSlug + "' already is " + name);
        }

        doSetAttribute(institution, true, set);
    }

    private void unsetAttribute(
            String institutionSlug,
            String name,
            BiConsumer<Institution, Boolean> set,
            Function<Institution, Boolean> get
    ) throws NotFoundException, InstitutionAttributeException {
        val institution = institutionRepository.findBySlug(institutionSlug)
                .orElseThrow(() -> new NotFoundException("Institution not found for id '" + institutionSlug + "'"));

        if (!get.apply(institution)) {
            throw new InstitutionAttributeException("Institution '" + institutionSlug + "' already isn't " + name);
        }

        // If a parent has attribute set, then one should manage that parent instead.
        val parent = institution.getParent();
        if (parent != null && get.apply(institution.getParent())) {
            throw new InstitutionAttributeException(
                    "Institution '" + institutionSlug + "' parent '" + parent.getSlug() + "' is " + name
            );
        }

        doSetAttribute(institution, false, set);
    }

    private void doSetAttribute(Institution institution, boolean value, BiConsumer<Institution, Boolean> set) {
        set.accept(institution, value);
        institution.getChildren().forEach(child -> doSetAttribute(child, value, set));
    }

    public <T> Set<T> getMembers(String institutionSlug, Class<T> projection) {
        return institutionRepository.findAllMembers(institutionSlug, projection);
    }

    public <T> List<T> getAll(Class<T> projection) {
        return institutionRepository.findAllBy(projection);
    }

    public <T> Optional<T> findBySlug(String slug, Class<T> projection) {
        return institutionRepository.findBySlug(slug, projection);
    }

    @Transactional(rollbackFor = {InstitutionUpdateException.class, NotFoundException.class})
    public String update(UpdateInstitutionRequest request, String institutionSlug)
            throws NotFoundException, S3ClientException {
        val institution = findBySlug(institutionSlug, Institution.class)
                .orElseThrow(() -> new NotFoundException("Institution not found for id '" + institutionSlug + "'"));

        institutionMapper.update(request, institution);

        if (request.getEmblem() != null) {
            if (fileStorage.exists(getEmblemKey(institutionSlug))) {
                fileStorage.delete(getEmblemKey(institutionSlug));
            }
            uploadEmblemIfRequested(institution.getSlug(), request.getEmblem());
        }

        return institution.getSlug();
    }

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
        return fileStorageProperties.getPathPrefix() + getEmblemKey(slug);
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
