package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.ProductType;

@Data
@Builder
public class BasicProductTypeResponse {
    private Long id;
    private String name;

    public static BasicProductTypeResponse of(ProductType productType) {
        return BasicProductTypeResponse.builder()
                .id(productType.getId())
                .name(productType.getName())
                .build();
    }
}
