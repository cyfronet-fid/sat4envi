package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.controller.request.WebhookRequest;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.util.S3Util;
import pl.cyfronet.s4e.util.TimeHelper;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SceneService {
    private final SceneRepository sceneRepository;
    private final ProductRepository productRepository;
    private final TimeHelper timeHelper;
    private final S3Util s3Util;

    public List<Scene> getScenes(Long productId, LocalDateTime start, LocalDateTime end) {
        return sceneRepository.findAllByProductIdAndTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAsc(
                productId, start, end
        );
    }

    public void saveScene(Scene scene) {
        sceneRepository.save(scene);
    }

    public Scene buildFromWebhook(WebhookRequest webhookRequest) throws NotFoundException {
        return Scene.builder().product(getProduct(webhookRequest.getKey()))
                .layerName(s3Util.getLayerName(webhookRequest.getKey()))
                .timestamp(s3Util.getTimeStamp(webhookRequest.getKey()))
                .s3Path(s3Util.getS3Path(webhookRequest.getKey())).build();
    }

    public Product getProduct(String key) throws NotFoundException {
        return productRepository.findByNameContainingIgnoreCase(s3Util.getProduct(key)).orElseThrow(() -> new NotFoundException());
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
}
