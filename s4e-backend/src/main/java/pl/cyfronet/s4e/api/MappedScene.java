package pl.cyfronet.s4e.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class MappedScene {
    private Long id;
    private Long productId;
    private String sceneKey;
    private String footprint;
    private String sceneContent;
    private String metadataContent;
    private LocalDateTime timestamp;
}
