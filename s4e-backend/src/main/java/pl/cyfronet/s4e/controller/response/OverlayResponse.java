package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Data
@Builder
public class OverlayResponse {
    @NotEmpty
    private Long id;
    @NotEmpty
    private String label;
    @NotEmpty
    private String layerName;
    @NotEmpty
    private String url;
    @NotEmpty
    private String ownerType;
    private String institutionSlug;
    @NotEmpty
    @Builder.Default
    private boolean visible = true;
    @NotEmpty
    private LocalDateTime createdAt;
}
