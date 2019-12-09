package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.UserRole;

@Data
@Builder
public class UserRoleResponse {
    private AppRole role;
    private String groupSlug;
    private String institutionSlug;

    public static UserRoleResponse of(UserRole role) {
        return UserRoleResponse.builder()
                .role(role.getRole())
                .groupSlug(role.getGroup().getSlug())
                .institutionSlug(role.getGroup().getInstitution().getSlug())
                .build();
    }
}
