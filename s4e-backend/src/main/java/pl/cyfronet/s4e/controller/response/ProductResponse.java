package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.Product;

import java.time.LocalDateTime;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private Long productTypeId;
    private LocalDateTime timestamp;
    private String layerName;

    public static ProductResponse of(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .productTypeId(product.getProductType().getId())
                .timestamp(product.getTimestamp())
                .layerName(product.getLayerName())
                .build();
    }
}
