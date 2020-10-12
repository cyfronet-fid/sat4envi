package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.data.repository.projection.ProjectionWithId;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.license.LicensePermissionEvaluator;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.util.TimeHelper;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SceneService {
    private final SceneRepository sceneRepository;
    private final ProductRepository productRepository;
    private final TimeHelper timeHelper;
    private final LicensePermissionEvaluator licensePermissionEvaluator;

    private interface ProductProjection {
        Long getId();
        Product.AccessType getAccessType();
    }

    public <T extends ProjectionWithId> List<T> list(
            Long productId,
            LocalDateTime start,
            LocalDateTime end,
            AppUserDetails userDetails,
            Class<T> projection
    ) throws NotFoundException {
        val sort = Sort.by("timestamp");

        ProductProjection product = productRepository.findById(productId, ProductProjection.class)
                .orElseThrow(() -> constructNFE("Product", productId));

        var scenesStream = sceneRepository.findAllInTimestampRangeForProduct(productId, start, end, sort, projection)
                .stream();

        // Only handle the EUMETSAT license: in the case of the OPEN license filtering is not necessary, and in the case
        // of the PRIVATE license no listing is allowed for unlicensed user (enforced by HttpSecurity).
        if (Product.AccessType.EUMETSAT.equals(product.getAccessType())) {
            scenesStream = scenesStream.filter(
                    scene -> licensePermissionEvaluator.allowSceneRead(scene.getId(), userDetails)
            );
        }

        return scenesStream.collect(Collectors.toUnmodifiableList());
    }

    public <T> Page<T> list(Pageable pageable, Class<T> projection) {
        return sceneRepository.findAllBy(pageable, projection);
    }

    public <T> Page<T> listByProduct(Long productId, Pageable pageable, Class<T> projection) throws NotFoundException {
        if (!productRepository.existsById(productId)) {
            throw constructNFE("Product", productId);
        }
        return sceneRepository.findAllByProductId(productId, pageable, projection);
    }

    public <T> Optional<T> findById(Long id, Class<T> projection) {
        return sceneRepository.findById(id, projection);
    }

    public void save(Scene scene) {
        sceneRepository.save(scene);
    }

    @Transactional
    public void delete(Long id) throws NotFoundException {
        if (!sceneRepository.existsById(id)) {
            throw constructNFE("Scene", id);
        }
        sceneRepository.deleteById(id);
    }

    @Transactional
    public void deleteProductScenes(Long productId) throws NotFoundException {
        if (!productRepository.existsById(productId)) {
            throw constructNFE("Product", productId);
        }
        sceneRepository.deleteAllByProductId(productId);
    }

    public List<LocalDate> getAvailabilityDates(Long productId, YearMonth yearMonth, ZoneId tz) {
        val dates = new ArrayList<LocalDate>();
        ZonedDateTime curr = ZonedDateTime.of(LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 1), LocalTime.MIDNIGHT, tz);
        ZonedDateTime end = curr.plusMonths(1);

        while (curr.isBefore(end)) {
            ZonedDateTime next = curr.plusDays(1);

            int count = sceneRepository.countAllByProductIdAndTimestampGreaterThanEqualAndTimestampLessThan(
                    productId, timeHelper.getLocalDateTimeInBaseZone(curr), timeHelper.getLocalDateTimeInBaseZone(next));

            if (count > 0) {
                dates.add(curr.toLocalDate());
            }

            curr = next;
        }

        return dates;
    }

    @Transactional(readOnly = true)
    public <T extends ProjectionWithId> Optional<T> getMostRecentScene(Long productId, AppUserDetails userDetails, Class<T> projection) throws NotFoundException {
        ProductProjection product = productRepository.findById(productId, ProductProjection.class)
                .orElseThrow(() -> constructNFE("Product", productId));

        Sort sort = Sort.by(Sort.Order.desc("timestamp"), Sort.Order.asc("id"));
        if (Product.AccessType.EUMETSAT.equals(product.getAccessType())) {
            // Stream all the Scenes in chunks of 4 (see query hint in repository) and stop when first matching
            // is encountered.
            // Most EUMETSAT products have 15 min temporal resolution,
            // this means 4 scenes per hour: on 0, 15, 30 and 45 min.
            // Then, fetching 4 scenes will always yield at least one on the hour scene (open to all)
            // and only a single chunk will be requested.
            return sceneRepository.streamAllByProductId(productId, sort, projection)
                .filter(scene -> licensePermissionEvaluator.allowSceneRead(scene.getId(), userDetails))
                .findFirst();
        }

        return sceneRepository.findFirstByProductId(productId, sort, projection);
    }

    private NotFoundException constructNFE(String name, Long id) {
        return new NotFoundException(name + " with id '" + id + "' not found");
    }
}
