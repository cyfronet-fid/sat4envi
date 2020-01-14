package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.controller.response.WMSOverlayResponse;
import pl.cyfronet.s4e.data.repository.WMSOverlayRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WMSOverlayService {
    private final WMSOverlayRepository wmsOverlayRepository;

    public List<WMSOverlayResponse> getWMSOverlays() {
        return wmsOverlayRepository.findAllBy(WMSOverlayResponse.class);
    }
}
