package pl.cyfronet.s4e.controller.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class ExpertHelpRequest {
    @NotNull
    HelpType helpType;

    @NotEmpty
    String issueDescription;
}
