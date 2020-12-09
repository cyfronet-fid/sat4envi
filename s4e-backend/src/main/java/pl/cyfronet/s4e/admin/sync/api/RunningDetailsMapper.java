package pl.cyfronet.s4e.admin.sync.api;

import lombok.val;
import org.mapstruct.Mapper;
import pl.cyfronet.s4e.admin.sync.task.SyncJob;
import pl.cyfronet.s4e.config.MapStructCentralConfig;

import java.io.PrintWriter;
import java.io.StringWriter;

@Mapper(config = MapStructCentralConfig.class)
public abstract class RunningDetailsMapper {
    public abstract ErrorsResponse toErrorsResponse(SyncJob.RunningDetails runningDetails);

    protected String toString(Exception cause) {
        if (cause == null) {
            return null;
        }

        val sw = new StringWriter();
        val pw = new PrintWriter(sw);
        cause.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }
}
