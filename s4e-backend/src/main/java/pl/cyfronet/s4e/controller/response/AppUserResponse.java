package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.AppUser;

@Data
@Builder
public class AppUserResponse {
    private String email;

    public static AppUserResponse of(AppUser user) {
        return AppUserResponse.builder()
                .email(user.getEmail())
                .build();
    }
}
