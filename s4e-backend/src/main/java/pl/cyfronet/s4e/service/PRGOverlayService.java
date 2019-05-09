package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.bean.PRGOverlay;
import pl.cyfronet.s4e.data.repository.PRGOverlayRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PRGOverlayService {
    private final PRGOverlayRepository prgOverlayRepository;

    public List<PRGOverlay> getCreatedPRGOverlays() {
        val out = new ArrayList<PRGOverlay>();
        prgOverlayRepository.findAllByCreatedTrue().forEach(out::add);
        return out;
    }
}
