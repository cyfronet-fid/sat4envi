package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.AppUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
public class MembersResponse {
    List<AppUserResponse> members;

    public static AppUserResponse of(AppUser user) {
        return AppUserResponse.builder()
                .email(user.getEmail())
                .build();
    }

    public static MembersResponse of(Set<AppUser> users) {
        return MembersResponse.builder()
                .members(new ArrayList<>(users.stream().map(u -> AppUserResponse.of(u)).collect(Collectors.toList())))
                .build();
    }
}
