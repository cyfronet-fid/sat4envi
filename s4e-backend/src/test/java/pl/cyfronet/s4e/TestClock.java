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
