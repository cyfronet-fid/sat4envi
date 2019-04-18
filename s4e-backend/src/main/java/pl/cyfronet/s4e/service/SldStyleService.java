package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.SldStyle;
import pl.cyfronet.s4e.data.repository.SldStyleRepository;

@Service
@RequiredArgsConstructor
public class SldStyleService {
    private final SldStyleRepository sldStyleRepository;

    @Transactional
    public SldStyle updateCreated(Long sldStyleId, boolean created) {
        SldStyle sldStyle = sldStyleRepository.findById(sldStyleId).get();
        sldStyle.setCreated(created);
        return sldStyleRepository.save(sldStyle);
    }
}
