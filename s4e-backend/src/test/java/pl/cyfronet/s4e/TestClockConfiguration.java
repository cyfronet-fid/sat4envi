package pl.cyfronet.s4e;

import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

public class TestClockConfiguration {
    @Bean
    public TestClock clock() {
        // The fact that the minutes part is equal to 0 is relevant for tests regarding EUMETSAT license.
        return new TestClock(LocalDateTime.of(2020, 1, 1, 0, 0));
    }
}
