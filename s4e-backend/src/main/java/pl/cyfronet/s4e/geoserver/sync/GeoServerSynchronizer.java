package pl.cyfronet.s4e.geoserver.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.PRGOverlay;
import pl.cyfronet.s4e.data.repository.PRGOverlayRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SldStyleRepository;
import pl.cyfronet.s4e.service.GeoServerService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoServerSynchronizer {
    private final SldStyleRepository sldStyleRepository;
    private final ProductRepository productRepository;
    private final PRGOverlayRepository prgOverlayRepository;

    private final GeoServerService geoServerService;

    @Transactional
    public void synchronizeStoreAndMosaics() {
        log.info("Creating stores and mosaics");
        for (val product : productRepository.findAll()) {
            if (!geoServerService.layerExists(product.getName().toLowerCase())) {
                geoServerService.addStoreAndLayer(product);
            }
        }
    }

    @Transactional
    public void synchronizeOverlays() {
        log.info("Creating styles");
        for (val sldStyle : sldStyleRepository.findAll()) {
            if (!sldStyle.isCreated()) {
                geoServerService.addStyle(sldStyle);
                sldStyle.setCreated(true);
            }
        }

        log.info("Creating PRG overlays");
        List<PRGOverlay> prgOverlays = new ArrayList<>();
        prgOverlayRepository.findAll().forEach(prgOverlays::add);
        // existence check is done inside
        geoServerService.createPrgOverlays(prgOverlays);
        for (val prgOverlay : prgOverlays) {
            if (geoServerService.layerExists(prgOverlay.getFeatureType())) {
                prgOverlay.setCreated(true);
            }
        }
    }
}
