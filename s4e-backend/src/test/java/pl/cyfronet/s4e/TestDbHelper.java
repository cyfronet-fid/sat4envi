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
    private final SchemaRepository schemaRepository;
    private final WMSOverlayRepository wmsOverlayRepository;
    private final PRGOverlayRepository prgOverlayRepository;
    private final SldStyleRepository sldStyleRepository;
    private final PropertyRepository propertyRepository;
    private final ProductCategoryRepository productCategoryRepository;

    public void clean() {
        productCategoryRepository.deleteAllByNameNot(ProductCategoryRepository.DEFAULT_CATEGORY_NAME);
        institutionRepository.deleteAll();
        appUserRepository.deleteAll();
        placeRepository.deleteAll();
        productRepository.deleteAll();
        schemaRepository.deleteAll();
        wmsOverlayRepository.deleteAll();
        prgOverlayRepository.deleteAll();
        sldStyleRepository.deleteAll();
        propertyRepository.deleteAll();
    }
}
