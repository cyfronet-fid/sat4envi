package pl.cyfronet.s4e.admin.sync.api;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SyncJobResponse {
    private String name;
    private String prefix;
    private boolean failFast;
    private String state;

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Extended extends SyncJobResponse {
        private List<StateDetailsResponse> stateHistory;
    }

    @Data
    public abstract static class StateDetailsResponse {
        private String state;
        private LocalDateTime since;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class PendingDetailsResponse extends StateDetailsResponse {
        private long sceneCount;
        private Duration sceneCountElapsed;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class RunningDetailsResponse extends StateDetailsResponse {
        private long successesCount;
        private long runningCount;
        private long errorsCount;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ElapsedDetailsResponse extends StateDetailsResponse {
        private Duration elapsed;
    }
}
