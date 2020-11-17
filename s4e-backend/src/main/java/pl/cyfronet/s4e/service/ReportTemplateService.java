package pl.cyfronet.s4e.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.data.repository.ReportTemplateRepository;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportTemplateService {
    @Value
    @Builder
    public static class CreateDTO {
        String ownerEmail;
        String caption;
        String notes;
        List<Long> overlayIds;
        Long productId;
    }

    private final ReportTemplateRepository reportTemplateRepository;
    private final ReportTemplateMapper reportTemplateMapper;

    @Transactional(rollbackFor = NotFoundException.class)
    public UUID create(CreateDTO createDTO) throws NotFoundException {
        val reportTemplate = reportTemplateMapper.dtoToPreEntity(createDTO);
        return reportTemplateRepository.save(reportTemplate).getId();
    }

    public void delete(UUID id) {
        reportTemplateRepository.deleteById(id);
    }

    public <T> Optional<T> findById(UUID id, Class<T> projection) {
        return reportTemplateRepository.findById(id, projection);
    }

    public <T> List<T> listByAppUser(String email, Class<T> projection) {
        return reportTemplateRepository.findAllByOwnerEmail(email, Sort.by("createdAt"), projection);
    }

    private interface OwnerEmailProjection {
        String getEmail();
    }

    public boolean canDelete(UUID id, Authentication authentication) {
        String email = authentication.getName();
        if (email == null) {
            return false;
        }
        return reportTemplateRepository.findOwnerOf(id, OwnerEmailProjection.class)
                .map(oep -> email.equals(oep.getEmail()))
                .orElse(false);
    }
}
