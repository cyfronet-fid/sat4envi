package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.util.TimeHelper;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SceneService {
    private final SceneRepository sceneRepository;
    private final TimeHelper timeHelper;

    public List<Scene> getScenes(Long productId, LocalDateTime start, LocalDateTime end) {
        return sceneRepository.findAllByProductIdAndTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAsc(
                productId, start, end
        );
    }

    public void saveScene(Scene scene) {
        sceneRepository.save(scene);
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
