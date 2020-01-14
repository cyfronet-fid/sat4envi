package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.controller.response.PRGOverlayResponse;
import pl.cyfronet.s4e.data.repository.PRGOverlayRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PRGOverlayService {
    private final PRGOverlayRepository prgOverlayRepository;

    public List<PRGOverlayResponse> getCreatedPRGOverlays() {
        return prgOverlayRepository.findAllByCreatedTrue();
    }
}
