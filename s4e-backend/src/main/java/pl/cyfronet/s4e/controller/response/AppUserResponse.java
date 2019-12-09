package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.AppUser;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
public class AppUserResponse {
    private String email;
    private String name;
    private String surname;
    private Set<UserRoleResponse> roles;

    public static AppUserResponse of(AppUser user) {
        return AppUserResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .surname(user.getSurname())
                .roles(user.getRoles().stream().map(UserRoleResponse::of).collect(Collectors.toSet()))
                .build();
    }
}
