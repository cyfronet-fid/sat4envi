package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.Group;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.controller.request.CreateChildInstitutionRequest;
import pl.cyfronet.s4e.controller.request.CreateInstitutionRequest;
import pl.cyfronet.s4e.controller.request.UpdateInstitutionRequest;
import pl.cyfronet.s4e.data.repository.GroupRepository;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.ex.InstitutionCreationException;
import pl.cyfronet.s4e.ex.InstitutionUpdateException;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstitutionService {

    public static final String DEFAULT = "default";
    public static final String PREFIX = "__";
    public static final String SUFFIX = "__";
    private final InstitutionRepository institutionRepository;
    private final GroupRepository groupRepository;
    private final UserRoleService userRoleService;
    private final SlugService slugService;

    @Transactional(rollbackFor = InstitutionCreationException.class)
    public void save(CreateInstitutionRequest request) throws InstitutionCreationException {
        save(Institution.builder()
                .name(request.getName())
                .slug(slugService.slugify(request.getName()))
                .parent(null)
                .build());
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
                .build());
        // add member to default group
        userRoleService.addRole(AppRole.GROUP_MEMBER, request.getInstitutionAdminEmail(), result.getSlug(), "default");
        // add institution_admin role for user
        userRoleService.addRole(AppRole.INST_ADMIN, request.getInstitutionAdminEmail(), result.getSlug(), "default");
        return result;
    }

    public <T> Optional<T> getInstitution(String slug, Class<T> projection) {
        return institutionRepository.findBySlug(slug, projection);
    }

    public <T> Page<T> getAll(Pageable pageable, Class<T> projection) {
        return institutionRepository.findAllBy(projection, pageable);
    }

    @Transactional(rollbackFor = {InstitutionUpdateException.class, NotFoundException.class})
    public void update(UpdateInstitutionRequest request, String institutionSlug) throws InstitutionUpdateException, NotFoundException {
        val institution = getInstitution(institutionSlug, Institution.class)
                .orElseThrow(() -> new NotFoundException("Institution not found for id '" + institutionSlug));
        institution.setName(request.getName());
        institution.setSlug(slugService.slugify(request.getName()));
        update(institution);
    }

    @Transactional(rollbackFor = InstitutionUpdateException.class)
    public void update(Institution institution) throws InstitutionUpdateException {
        try {
            institutionRepository.save(institution);
        } catch (DataIntegrityViolationException e) {
            log.info("Cannot create Institution with name '" + institution.getName() + "'", e);
            throw new InstitutionUpdateException("Cannot update Institution", e);
        }
    }

    @Transactional
    public void delete(String slug) {
        institutionRepository.deleteInstitutionBySlug(slug);
    }
}
