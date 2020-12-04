package pl.cyfronet.s4e;

import org.awaitility.Awaitility;
import org.awaitility.Durations;

public class TestAwaitilityConfiguration {

    public static void initialize() {
    }

    static {
        Awaitility.setDefaultTimeout(Durations.TEN_SECONDS);
    }
}
