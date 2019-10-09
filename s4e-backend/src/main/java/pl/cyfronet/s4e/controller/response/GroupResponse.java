package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.Group;

@Data
@Builder
public class GroupResponse {
    private String name;
    private String slug;

    public static GroupResponse of(Group group) {
        return GroupResponse.builder()
                .name(group.getName())
                .slug(group.getSlug())
                .build();
    }
}
