package pl.cyfronet.s4e.controller.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserMeResponse {
    Long id;
    String email;
    String name;
    String surname;

    boolean admin;
    boolean memberZK;
    Set<String> authorities;

    Set<UserRoleResponse> roles;

    String domain;
    String usage;
    String country;
}
