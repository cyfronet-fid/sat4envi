package pl.cyfronet.s4e.controller.response;

import java.util.Set;

public interface AppUserResponse {
    String getEmail();

    String getName();

    boolean getAdmin();

    boolean getMemberZK();

    String getSurname();

    Set<UserRoleResponse> getRoles();

    String getDomain();

    String getUsage();

    String getCountry();
}
