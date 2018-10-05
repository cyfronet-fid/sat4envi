package pl.cyfronet.s4e.granules;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GranuleService {
    private final GranuleRepository granuleRepository;

    public List<Granule> getGranules(Long productId) {
        return granuleRepository.findByProductId(productId);
    }
}
