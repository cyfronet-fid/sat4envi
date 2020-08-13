package pl.cyfronet.s4e.admin.sync.api;

import org.mapstruct.Mapper;
import pl.cyfronet.s4e.admin.sync.task.SyncJob;
import pl.cyfronet.s4e.config.MapStructCentralConfig;

@Mapper(config = MapStructCentralConfig.class)
public abstract class RunningDetailsMapper {
    public abstract ErrorsResponse toErrorsResponse(SyncJob.RunningDetails runningDetails);
}
