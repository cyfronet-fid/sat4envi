package pl.cyfronet.s4e.admin.sync;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
public class AdminSceneSyncRequest {
    @NotEmpty
    private String prefix;
}
