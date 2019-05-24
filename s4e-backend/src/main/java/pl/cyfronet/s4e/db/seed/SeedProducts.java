package pl.cyfronet.s4e.db.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.bean.PRGOverlay;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.ProductType;
import pl.cyfronet.s4e.bean.SldStyle;
import pl.cyfronet.s4e.data.repository.PRGOverlayRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.ProductTypeRepository;
import pl.cyfronet.s4e.data.repository.SldStyleRepository;
import pl.cyfronet.s4e.geoserver.sync.GeoServerSynchronizer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Profile({"development", "run-seed-products"})
@Component
@RequiredArgsConstructor
@Slf4j
public class SeedProducts implements ApplicationRunner {
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2018, 10, 4, 0, 0);

    @Value("${seed.products.seed-db:true}")
    private boolean seedDb;

    @Value("${seed.products.sync-geoserver:true}")
    private boolean syncGeoserver;

    private final ProductTypeRepository productTypeRepository;
    private final ProductRepository productRepository;
    private final SldStyleRepository sldStyleRepository;
    private final PRGOverlayRepository prgOverlayRepository;

    private final GeoServerSynchronizer geoServerSynchronizer;

    @Async
    @Override
    public void run(ApplicationArguments args) {
        if (seedDb) {
            productRepository.deleteAll();
            productTypeRepository.deleteAll();
            prgOverlayRepository.deleteAll();
            sldStyleRepository.deleteAll();

            seedProducts();
            seedOverlays();
        }

        if (syncGeoserver) {
            if (!seedDb) {
                setAllProductsNotCreated();
            }
            geoServerSynchronizer.resetWorkspace();
            geoServerSynchronizer.synchronizeOverlays();
            geoServerSynchronizer.synchronizeProducts();
        }

        log.info("Seeding complete");
    }

    private void setAllProductsNotCreated() {
        for (val product: productRepository.findAll()) {
            product.setCreated(false);
            productRepository.save(product);
        }
    }

    private void seedProducts() {
        log.info("Seeding ProductTypes");
        List<ProductType> productTypes = Arrays.asList(new ProductType[]{
                ProductType.builder()
                        .name("108m")
                        .description("Obraz satelitarny Meteosat dla obszaru Europy w kanale 10.8 µm z zastosowanie maskowanej palety barw dla obszarów mórz i lądów.")
                        .build(),
                ProductType.builder()
                        .name("Setvak")
                        .description("Obraz satelitarny Meteosat w kanale 10.8 µm z paletą barwną do analizy powierzchni wysokich chmur konwekcyjnych – obszar Europy Centralnej.")
                        .build(),
                ProductType.builder()
                        .name("WV-IR")
                        .description("Opis produktu WV-IR.")
                        .build(),
        });
        productTypeRepository.saveAll(productTypes);

        log.info("Seeding Products");
        val products = new ArrayList<Product>();
        products.addAll(generateProducts(productTypes.get(0), "_Merkator_Europa_ir_108m"));
        products.addAll(generateProducts(productTypes.get(1), "_Merkator_Europa_ir_108_setvak"));
        products.addAll(generateProducts(productTypes.get(2), "_Merkator_WV-IR"));
        productRepository.saveAll(products);
    }

    private void seedOverlays() {
        log.info("Seeding SldStyles");
        val sldStyles = new ArrayList<SldStyle>();
        sldStyles.add(SldStyle.builder()
                .name("wojewodztwa")
                .build());
        sldStyleRepository.saveAll(sldStyles);

        log.info("Seeding PRGOverlays");
        val prgOverlays = new ArrayList<PRGOverlay>();
        prgOverlays.add(PRGOverlay.builder()
                .name("wojewodztwa")
                .featureType("wojewodztwa")
                .sldStyle(sldStyles.get(0))
                .build());
        prgOverlayRepository.saveAll(prgOverlays);
    }

    private List<Product> generateProducts(ProductType productType, String suffix) {
        val count = 24;
        val granules = new ArrayList<Product>();
        for (int i = 0; i < count; i++) {
            LocalDateTime timestamp = BASE_TIME.plusHours(i);
            String layerName = DateTimeFormatter.ofPattern("yyyyMMddHHmm").format(timestamp) + suffix;
            granules.add(Product.builder()
                    .productType(productType)
                    .timestamp(timestamp)
                    .layerName(layerName)
                    .s3Path(layerName+".tif")
                    // if we're not syncing GeoServer then assume it is populated
                    .created(!syncGeoserver)
                    .build());
        }
        return granules;
    }
}
