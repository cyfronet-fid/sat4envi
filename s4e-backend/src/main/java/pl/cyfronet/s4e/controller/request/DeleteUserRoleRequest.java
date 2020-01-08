package pl.cyfronet.s4e.controller.request;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.AppRole;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class DeleteUserRoleRequest {
    @NotEmpty
    @Email
    String email;
    @NotNull
    AppRole role;
    @NotEmpty
    String institutionSlug;
    @NotEmpty
    String groupSlug;
}
