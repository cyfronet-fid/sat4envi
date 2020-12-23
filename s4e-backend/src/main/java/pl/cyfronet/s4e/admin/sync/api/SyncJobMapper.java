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

import org.mapstruct.Mapper;
import pl.cyfronet.s4e.admin.sync.task.SyncJob;
import pl.cyfronet.s4e.config.MapStructCentralConfig;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(config = MapStructCentralConfig.class)
public abstract class SyncJobMapper {
    public abstract SyncJobResponse toResponse(SyncJob syncJob);

    public abstract SyncJobResponse.Extended toExtendedResponse(SyncJob syncJob);

    private SyncJobResponse.StateDetailsResponse toResponse(SyncJob.StateDetails stateDetails) {
        if (stateDetails instanceof SyncJob.PendingDetails) {
            return toResponse((SyncJob.PendingDetails) stateDetails);
        } else if (stateDetails instanceof SyncJob.RunningDetails) {
            return toResponse((SyncJob.RunningDetails) stateDetails);
        } else if (stateDetails instanceof SyncJob.ElapsedDetails) {
            return toResponse((SyncJob.ElapsedDetails) stateDetails);
        } else {
            return null;
        }
    }

    protected abstract SyncJobResponse.PendingDetailsResponse toResponse(SyncJob.PendingDetails pendingDetails);

    protected abstract SyncJobResponse.RunningDetailsResponse toResponse(SyncJob.RunningDetails runningDetails);

    protected abstract SyncJobResponse.ElapsedDetailsResponse toResponse(SyncJob.ElapsedDetails elapsedDetails);

    protected List<SyncJobResponse.StateDetailsResponse> mapStateHistory(Map<SyncJob.State, SyncJob.StateDetails> stateHistory) {
        return stateHistory.values().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
