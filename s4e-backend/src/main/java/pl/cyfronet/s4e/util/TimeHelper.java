package pl.cyfronet.s4e.util;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class TimeHelper {
    private static final ZoneId BASE_ZONE_ID = ZoneId.of("UTC");

    public ZonedDateTime getZonedDateTimeWithBaseZone(LocalDateTime localDateTime) {
        return ZonedDateTime.of(localDateTime, BASE_ZONE_ID);
    }

    /**
     * Get a ZonedDateTime in zoneId zone, with localDateTime assumed to be in UTC.
     *
     * @param localDateTime assumed to be in UTC
     * @param zoneId target zone
     * @return a ZonedDateTime with localDateTime instant in zoneId zone
     */
    public ZonedDateTime getZonedDateTime(LocalDateTime localDateTime, ZoneId zoneId) {
        return getZonedDateTimeWithBaseZone(localDateTime).withZoneSameInstant(zoneId);
    }

    /**
     * Get a LocalDateTime of the zonedDateTime converted to UTC.
     *
     * @param zonedDateTime the source date-time
     * @return a LocalDateTime with the same instant in UTC
     */
    public LocalDateTime getLocalDateTimeInBaseZone(ZonedDateTime zonedDateTime) {
        return zonedDateTime.withZoneSameInstant(BASE_ZONE_ID).toLocalDateTime();
    }
}
