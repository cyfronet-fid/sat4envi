package pl.cyfronet.s4e.controller.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
public class UpdateGroupRequest {
    @NotEmpty
    private String name;
}