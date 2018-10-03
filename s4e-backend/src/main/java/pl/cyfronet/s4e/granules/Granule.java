package pl.cyfronet.s4e.granules;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Granule {
    private Long id;
    private Long productId;
    private LocalDateTime timestamp;
    private String layerName;
}
