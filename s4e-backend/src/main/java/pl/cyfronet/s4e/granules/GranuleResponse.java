package pl.cyfronet.s4e.granules;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GranuleResponse {
    private Long id;
    private Long productId;
    private LocalDateTime timestamp;
    private String layerName;

    public static GranuleResponse of(Granule granule) {
        return GranuleResponse.builder()
                .id(granule.getId())
                .productId(granule.getProduct().getId())
                .timestamp(granule.getTimestamp())
                .layerName(granule.getLayerName())
                .build();
    }
}
