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
    public void setZk(String institutionSlug) throws InstitutionZkException, NotFoundException {
        val institution = institutionRepository.findBySlug(institutionSlug)
                .orElseThrow(() -> new NotFoundException("Institution not found for id '" + institutionSlug + "'"));

        if (institution.isZk()) {
            throw new InstitutionZkException("Institution '" + institutionSlug + "' already is ZK");
        }

        doSetZk(true, institution);
    }

    @Transactional
    public void unsetZk(String institutionSlug) throws NotFoundException, InstitutionZkException {
        val institution = institutionRepository.findBySlug(institutionSlug)
                .orElseThrow(() -> new NotFoundException("Institution not found for id '" + institutionSlug + "'"));

        if (!institution.isZk()) {
            throw new InstitutionZkException("Institution '" + institutionSlug + "' already isn't ZK");
        }
        val parent = institution.getParent();
        if (parent != null && parent.isZk()) {
            throw new InstitutionZkException(
                    "Institution '" + institutionSlug + "' parent '" + parent.getSlug() + "' is ZK"
            );
        }

        doSetZk(false, institution);
    }

    private void doSetZk(boolean zk, Institution institution) {
        institution.setZk(zk);
        institution.getChildren().forEach(child -> doSetZk(zk, child));
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
