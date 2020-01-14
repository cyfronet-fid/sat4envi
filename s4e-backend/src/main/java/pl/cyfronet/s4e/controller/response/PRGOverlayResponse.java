package pl.cyfronet.s4e.controller.response;

import org.springframework.beans.factory.annotation.Value;

public interface PRGOverlayResponse {
    Long getId();

    String getName();

    @Value("#{target.featureType}")
    String getLayerName();
}
