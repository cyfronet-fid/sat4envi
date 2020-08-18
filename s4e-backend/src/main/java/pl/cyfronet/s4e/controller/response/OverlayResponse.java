package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OverlayResponse {
    private Long id;
    private String label;
    private String url;
    private String ownerType;
    private boolean visible;
    private LocalDateTime createdAt;
}
