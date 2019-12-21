package pl.cyfronet.s4e.controller.request;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.AppRole;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class UpdateUserGroupsRequest {
    @Email
    @NotEmpty
    private String email;

    private Map<String, Set<AppRole>> groupsWithRoles;
}
