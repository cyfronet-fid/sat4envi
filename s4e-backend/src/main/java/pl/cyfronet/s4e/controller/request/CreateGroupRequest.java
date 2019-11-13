package pl.cyfronet.s4e.controller.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Data
@Builder
public class CreateGroupRequest {
    @NotEmpty
    private String name;
    private Set<String> membersEmails;
}