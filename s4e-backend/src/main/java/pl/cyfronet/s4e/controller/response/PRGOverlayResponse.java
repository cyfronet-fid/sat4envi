package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.PRGOverlay;

@Data
@Builder
public class PRGOverlayResponse {
    private Long id;
    private String name;
    private String sldStyleName;

    public static PRGOverlayResponse of(PRGOverlay overlay){
        return PRGOverlayResponse.builder()
                .id(overlay.getId())
                .name(overlay.getName())
                .sldStyleName(overlay.getSldStyle().getName())
                .build();
    }
}
