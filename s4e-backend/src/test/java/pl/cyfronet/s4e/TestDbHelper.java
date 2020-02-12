package pl.cyfronet.s4e;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.data.repository.*;

@Service
@RequiredArgsConstructor
public class TestDbHelper {
    private final InstitutionRepository institutionRepository;
    private final AppUserRepository appUserRepository;
    private final PlaceRepository placeRepository;
    private final ProductRepository productRepository;

    public void clean() {
        institutionRepository.deleteAll();
        appUserRepository.deleteAll();
        placeRepository.deleteAll();
        productRepository.deleteAll();
    }
}
