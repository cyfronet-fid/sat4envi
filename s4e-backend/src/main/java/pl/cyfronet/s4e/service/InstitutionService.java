package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Group;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.data.repository.GroupRepository;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.ex.InstitutionCreationException;
import pl.cyfronet.s4e.ex.InstitutionUpdateException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstitutionService {

    private static final String DEFAULT = "default";
    private static final String PREFIX = "__";
    private static final String SUFFIX = "__";
    private final InstitutionRepository institutionRepository;
    private final GroupRepository groupRepository;

    @Transactional(rollbackFor = InstitutionCreationException.class)
    public Institution save(Institution institution) throws InstitutionCreationException {
        try {
            Institution result = institutionRepository.save(institution);
            Group group = Group.builder().institution(result).name(PREFIX+DEFAULT+SUFFIX).slug(DEFAULT).build();
            groupRepository.save(group);
            return result;
        } catch (DataIntegrityViolationException e) {
            log.info("Cannot create Institution with name '" + institution.getName() + "'", e);
            throw new InstitutionCreationException("Cannot create Institution", e);
        }
    }

    public Optional<Institution> getInstitution(String slug) {
        return institutionRepository.findBySlug(slug);
    }

    public Page<Institution> getAll(Pageable pageable) {
        return institutionRepository.findAll(pageable);
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
