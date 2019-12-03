package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.Product;

@Data
@Builder
public class BasicProductResponse {
    private Long id;
    private String name;

    public static BasicProductResponse of(Product product) {
        return BasicProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .build();
    }
}
