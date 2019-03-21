package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.WMSOverlay;

@Data
@Builder
public class WMSOverlayResponse {
    private Long id;
    private String name;
    private String url;

    public static WMSOverlayResponse of(WMSOverlay overlay) {
        return WMSOverlayResponse.builder()
                .id(overlay.getId())
                .name(overlay.getName())
                .url(overlay.getUrl())
                .build();
    }
}
