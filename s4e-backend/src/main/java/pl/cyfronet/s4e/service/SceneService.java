package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Webhook;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.util.S3Util;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SceneService {
    private final SceneRepository sceneRepository;
    private final ProductRepository productRepository;
    private final S3Util s3Util;

    public List<Scene> getScenes(Long productId) {
        return sceneRepository.findByProductId(productId);
    }

    public List<Scene> getScenes(Long productId, LocalDateTime start, LocalDateTime end) {
        return sceneRepository.findAllByProductIdAndTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAsc(
                productId, start, end
        );
    }

    public void saveScene(Scene scene) {
        sceneRepository.save(scene);
    }

    public Scene buildFromWebhook(Webhook webhook) throws NotFoundException {
        return Scene.builder().product(getProduct(webhook.getKey()))
                .layerName(s3Util.getLayerName(webhook.getKey()))
                .timestamp(s3Util.getTimeStamp(webhook.getKey()))
                .s3Path(s3Util.getS3Path(webhook.getKey())).build();
    }

    public Product getProduct(String key) throws NotFoundException {
        return productRepository.findByNameContainingIgnoreCase(s3Util.getProduct(key)).orElseThrow(() -> new NotFoundException());
    }

    public List<LocalDate> getAvailabilityDates(Long productId, YearMonth yearMonth) {
        val start = LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonth(), 1, 0, 0);
        return sceneRepository.findDatesWithData(productId, start, start.plusMonths(1)).stream()
                .map(Date::toLocalDate)
                .collect(Collectors.toList());
    }
}
