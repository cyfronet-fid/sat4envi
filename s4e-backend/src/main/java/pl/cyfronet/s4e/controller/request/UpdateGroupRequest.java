package pl.cyfronet.s4e.controller.request;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.controller.validation.GroupNameValid;

import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Data
@Builder
public class UpdateGroupRequest {
    @NotEmpty
    @GroupNameValid
    private String name;
    private Set<String> membersEmails;
}
