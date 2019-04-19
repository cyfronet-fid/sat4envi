package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.PRGOverlay;
import pl.cyfronet.s4e.data.repository.PRGOverlayRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PRGOverlayService {
    private final PRGOverlayRepository prgOverlayRepository;

    public List<PRGOverlay> getPRGOverlays() {
        val out = new ArrayList<PRGOverlay>();
        prgOverlayRepository.findAll().forEach(out::add);
        return out;
    }

    public List<PRGOverlay> getCreatedPRGOverlays() {
        val out = new ArrayList<PRGOverlay>();
        prgOverlayRepository.findAllByCreatedTrue().forEach(out::add);
        return out;
    }

    @Transactional
    public PRGOverlay updateCreated(Long prgOverlayId, boolean created) {
        PRGOverlay prgOverlay = prgOverlayRepository.findById(prgOverlayId).get();
        prgOverlay.setCreated(created);
        return prgOverlayRepository.save(prgOverlay);
    }
}
