package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.ex.InstitutionCreationException;
import pl.cyfronet.s4e.ex.InstitutionUpdateException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstitutionService {

    private final InstitutionRepository institutionRepository;

    @Transactional(rollbackFor = InstitutionCreationException.class)
    public Institution save(Institution institution) throws InstitutionCreationException {
        try {
            return institutionRepository.save(institution);
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
