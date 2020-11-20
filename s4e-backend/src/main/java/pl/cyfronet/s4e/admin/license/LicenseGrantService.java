package pl.cyfronet.s4e.admin.license;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import pl.cyfronet.s4e.bean.LicenseGrant;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.data.repository.LicenseGrantRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.ex.LicenseGrantException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LicenseGrantService {
    private static final String INSTITUTION_NOT_FOUND_CODE = "pl.cyfronet.s4e.admin.license.LicenseGrantService.institution-not-found";
    private static final String PRODUCT_NOT_FOUND_CODE = "pl.cyfronet.s4e.admin.license.LicenseGrantService.product-not-found";
    private static final String PRODUCT_NOT_PRIVATE_CODE = "pl.cyfronet.s4e.admin.license.LicenseGrantService.product-not-private";
    private static final String LICENSE_GRANT_ALREADY_EXISTS_CODE = "pl.cyfronet.s4e.admin.license.LicenseGrantService.already-exists";
    private static final String CANNOT_DELETE_OWNER_CODE = "pl.cyfronet.s4e.admin.license.LicenseGrantService.cannot-delete-owner";

    private final LicenseGrantRepository licenseGrantRepository;

    private final ProductRepository productRepository;

    private final InstitutionRepository institutionRepository;

    private final ObjectMapper objectMapper;

    @Data
    @Builder
    public static class CreateDTO {
        private String institutionSlug;

        private Long productId;

        private boolean owner;
    }

    @Transactional
    public Long create(CreateDTO dto) throws LicenseGrantException {
        val licenseGrantBuilder = LicenseGrant.builder();

        BindingResult bindingResult = new MapBindingResult(objectMapper.convertValue(dto, Map.class), "createDTO");

        if (licenseGrantRepository.existsByProductIdAndInstitutionSlug(dto.getProductId(), dto.getInstitutionSlug())) {
            bindingResult.reject(LICENSE_GRANT_ALREADY_EXISTS_CODE);
        }

        {
            val institutionSlug = dto.getInstitutionSlug();
            institutionRepository.findBySlug(institutionSlug)
                    .ifPresentOrElse(
                            licenseGrantBuilder::institution,
                            () -> bindingResult.rejectValue("institutionSlug", INSTITUTION_NOT_FOUND_CODE)
                    );
        }

        {
            val productId = dto.getProductId();
            val product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                bindingResult.rejectValue("productId", PRODUCT_NOT_FOUND_CODE);
            } else if (product.getAccessType() != Product.AccessType.PRIVATE) {
                bindingResult.rejectValue("productId", PRODUCT_NOT_PRIVATE_CODE);
            } else {
                licenseGrantBuilder.product(product);
            }
        }

        if (bindingResult.hasErrors()) {
            throw new LicenseGrantException(bindingResult);
        }

        licenseGrantBuilder.owner(dto.isOwner());

        return licenseGrantRepository.save(licenseGrantBuilder.build()).getId();
    }

    @Transactional
    public <T> Optional<T> updateOwner(Long id, boolean owner, Class<T> projection) {
        return licenseGrantRepository.findById(id)
                .map(licenseGrant -> {
                    licenseGrant.setOwner(owner);
                    return licenseGrantRepository.findByIdFetchInstitutionAndProduct(id, projection).get();
                });
    }

    @Transactional
    public boolean delete(Long id) {
        return licenseGrantRepository.findById(id)
                .map(licenseGrant -> {
                    licenseGrantRepository.delete(licenseGrant);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public boolean delete(Long productId, String institutionSlug) throws LicenseGrantException {
        val licenseGrant = licenseGrantRepository.findByProductIdAndInstitutionSlug(productId, institutionSlug)
                .orElse(null);

        if (licenseGrant == null) {
            return false;
        }

        if (licenseGrant.isOwner()) {
            BindingResult bindingResult = new MapBindingResult(Map.of(), "delete");
            bindingResult.reject(CANNOT_DELETE_OWNER_CODE);
            throw new LicenseGrantException(bindingResult);
        }

        licenseGrantRepository.delete(licenseGrant);
        return true;
    }

    public <T> Optional<T> findByIdFetchInstitutionAndProduct(Long id, Class<T> projection) {
        return licenseGrantRepository.findByIdFetchInstitutionAndProduct(id, projection);
    }

    public <T> List<T> findAllFetchInstitutionAndProduct(Class<T> projection) {
        return licenseGrantRepository.findAllFetchInstitutionAndProduct(projection);
    }

    @Transactional(readOnly = true)
    public <T> Optional<List<T>> findAllByProductIdFetchInstitutionAndProduct(Long productId, Class<T> projection) {
        if (!productRepository.existsById(productId)) {
            return Optional.empty();
        }

        return Optional.of(
                licenseGrantRepository.findAllByProductIdFetchInstitutionAndProduct(productId, projection)
        );
    }

    public <T> Optional<List<T>> findAllByInstitutionSlugFetchInstitutionAndProduct(String institutionSlug, Class<T> projection) {
        if (!institutionRepository.existsBySlug(institutionSlug)) {
            return Optional.empty();
        }

        return Optional.of(
                licenseGrantRepository.findAllByInstitutionSlugFetchInstitutionAndProduct(institutionSlug, projection)
        );
    }
}
