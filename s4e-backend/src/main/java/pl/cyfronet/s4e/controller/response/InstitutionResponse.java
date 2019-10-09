package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.Institution;

@Data
@Builder
public class InstitutionResponse {
    private String name;
    private String slug;

    public static InstitutionResponse of(Institution institution) {
        return InstitutionResponse.builder()
                .name(institution.getName())
                .slug(institution.getSlug())
                .build();
    }
}
