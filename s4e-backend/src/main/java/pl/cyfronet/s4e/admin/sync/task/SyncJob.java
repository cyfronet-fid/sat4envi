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

package pl.cyfronet.s4e.admin.sync.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.cyfronet.s4e.sync.Error;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Future;

@Getter
public class SyncJob {
    private final String name;
    private final String prefix;
    private final boolean failFast;
    private final Map<State, StateDetails> stateHistory = new LinkedHashMap<>();
    @Setter private State state = State.PENDING;

    public SyncJob(String name, String prefix, boolean failFast) {
        this.name = name;
        this.prefix = prefix;
        this.failFast = failFast;
    }

    public StateDetails getDetails(State state) {
        return stateHistory.get(state);
    }

    public void addState(StateDetails stateDetails) {
        stateHistory.put(stateDetails.getState(), stateDetails);
    }

    public enum State {
        PENDING, RUNNING, FINISHED, CANCELLED
    }

    @RequiredArgsConstructor
    @Getter
    public static abstract class StateDetails {
        private final State state;
        private final LocalDateTime since;
    }

    @Getter @Setter
    public static class PendingDetails extends StateDetails {
        private long sceneCount;
        private Duration sceneCountElapsed;

        public PendingDetails(LocalDateTime since) {
            super(State.PENDING, since);
        }
    }

    public static class RunningDetails extends StateDetails {
        @Getter @Setter
        private long successesCount = 0;
        @Getter @Setter
        private long runningCount = 0;
        private final List<Error> errors = new ArrayList<>();
        @Getter @Setter
        private Future<?> future;

        public RunningDetails(LocalDateTime since) {
            super(State.RUNNING, since);
        }

        public void addError(Error error) {
            errors.add(error);
        }

        public List<Error> getErrors() {
            return List.copyOf(errors);
        }

        public long getErrorsCount() {
            return errors.size();
        }
    }

    public static class ElapsedDetails extends StateDetails {
        @Getter
        private final Duration elapsed;

        public ElapsedDetails(State state, LocalDateTime since, Duration elapsed) {
            super(state, since);
            this.elapsed = elapsed;
        }
    }
}
