package pl.cyfronet.s4e;

import lombok.experimental.Delegate;

import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAmount;

public class TestClock extends Clock {
    @Delegate
    private Clock instance;

    public TestClock(@NotNull LocalDateTime localDateTime) {
        set(localDateTime);
    }

    public void set(LocalDateTime localDateTime) {
        instance = Clock.fixed(localDateTime.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
    }

    public void forward(TemporalAmount duration) {
        instance = Clock.fixed(instance.instant().plus(duration), instance.getZone());
    }
}
