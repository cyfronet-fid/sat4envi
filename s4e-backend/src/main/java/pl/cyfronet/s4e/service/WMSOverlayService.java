package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.bean.WMSOverlay;
import pl.cyfronet.s4e.data.repository.WMSOverlayRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WMSOverlayService {

    private final WMSOverlayRepository wmsOverlayRepository;

    public List<WMSOverlay> getWMSOverlays() {
        val out = new ArrayList<WMSOverlay>();
        wmsOverlayRepository.findAll().forEach(out::add);
        return out;
    }
}
