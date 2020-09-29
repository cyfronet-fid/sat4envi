package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.util.TimeHelper;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SceneService {
    private final SceneRepository sceneRepository;
    private final ProductRepository productRepository;
    private final TimeHelper timeHelper;

    public <T> List<T> list(Long productId, LocalDateTime start, LocalDateTime end, Class<T> projection) throws NotFoundException {
        if (!productRepository.existsById(productId)) {
            throw constructNFE("Product", productId);
        }
        return sceneRepository.findAllByProductIdAndTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAsc(
                productId, start, end, projection
        );
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

    public <T> Optional<T> getMostRecentScene(Long productId, Class<T> projection) throws NotFoundException {
        if (!productRepository.existsById(productId)) {
            throw constructNFE("Product", productId);
        }
        Sort sort = Sort.by(Sort.Order.desc("timestamp"), Sort.Order.asc("id"));
        return sceneRepository.findFirstByProductId(productId, sort, projection);
    }

    private NotFoundException constructNFE(String name, Long id) {
        return new NotFoundException(name + " with id '" + id + "' not found");
    }
}
