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
