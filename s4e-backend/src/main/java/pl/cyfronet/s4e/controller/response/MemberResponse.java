package pl.cyfronet.s4e.controller.response;

import java.util.Set;

public interface MemberResponse {
    Long getId();

    String getEmail();

    String getName();

    String getSurname();

    Set<UserRoleResponse> getRoles();
}
