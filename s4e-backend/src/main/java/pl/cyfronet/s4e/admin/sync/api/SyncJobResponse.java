/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
