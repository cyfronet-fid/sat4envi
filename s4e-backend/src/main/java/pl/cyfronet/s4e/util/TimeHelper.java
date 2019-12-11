package pl.cyfronet.s4e.util;

import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.Constants;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Service
public class TimeHelper {
    public ZonedDateTime getZonedDateTimeWithDefaultZone(LocalDateTime localDateTime) {
        return ZonedDateTime.of(localDateTime, Constants.ZONE_ID);
    }
}
