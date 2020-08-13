package pl.cyfronet.s4e.admin.sync.api;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
@Builder
public class CreateSyncJobRequest {
    @NotEmpty
    private String name;

    @NotEmpty
    @Pattern(regexp = "^[^/].*")
    private String prefix;

    @Builder.Default
    private boolean failFast = false;
}
