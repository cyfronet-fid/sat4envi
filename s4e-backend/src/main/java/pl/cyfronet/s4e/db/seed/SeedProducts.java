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
import pl.cyfronet.s4e.service.GeoServerService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Profile({"development & !skip-seed-products", "run-seed-products"})
@Component
@RequiredArgsConstructor
@Slf4j
public class SeedProducts implements ApplicationRunner {
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2018, 10, 4, 0, 0);
    private static final DateTimeFormatter DATE_TIME_PATTERN = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    @Value("${seed.products.seed-db:true}")
    private boolean seedDb;

    @Value("${seed.products.sync-geoserver:true}")
    private boolean syncGeoserver;

    @Value("${seed.products.days:10000}")
    private long days;

    private final ProductTypeRepository productTypeRepository;
    private final ProductRepository productRepository;
    private final SldStyleRepository sldStyleRepository;
    private final PRGOverlayRepository prgOverlayRepository;

    private final GeoServerService geoServerService;
    private final GeoServerSynchronizer geoServerSynchronizer;

    @Async
    @Override
    public void run(ApplicationArguments args) {
//        if (syncGeoserver) {
//            try {
//                geoServerSynchronizer.resetWorkspace();
//            } catch (Exception e) {
//                log.warn(e.getMessage(), e);
//            }
//        }

        if (seedDb) {
            productRepository.deleteAll();
            productTypeRepository.deleteAll();
            prgOverlayRepository.deleteAll();
            sldStyleRepository.deleteAll();

            seedProducts();
            seedOverlays();
        }

        if (syncGeoserver) {
            try {
                geoServerSynchronizer.synchronizeOverlays();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }

        log.info("Seeding complete");
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

        log.info("Seeding Products, base time: "+BASE_TIME.toString());
        seedProducts(productTypes.get(0), "_Merkator_Europa_ir_108m");
        seedProducts(productTypes.get(1), "_Merkator_Europa_ir_108_setvak");
        seedProducts(productTypes.get(2), "_Merkator_WV-IR");
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

    private void seedProducts(ProductType productType, String suffix) {
        long count = 24 * days;
        log.info("Seeding products of product type '"+productType.getName()+"', "+count+" total");
        for (long i = 0; i < count; i++) {
            val timestamp = BASE_TIME.plusHours(i);
            val layerName = DATE_TIME_PATTERN.format(timestamp) + suffix;

            val timestampModuloDay = BASE_TIME.plusHours(i % 24); // The timestamp we actually have data for.
            val s3Path = DATE_TIME_PATTERN.format(timestampModuloDay) + suffix + ".tif";

            val product = Product.builder()
                    .productType(productType)
                    .timestamp(timestamp)
                    .layerName(layerName)
                    .s3Path(s3Path)
                    .build();

            productRepository.save(product);

            if (syncGeoserver) {
                int retryCount = 3;
                while (retryCount-- > 0) {
                    try {
                        if (!geoServerService.layerExists(product.getLayerName())) {
                            geoServerService.addLayer(product);
                        }
                        break;
                    } catch (Exception e) {
                        log.warn(String.format("Retrying. %d retries left. %s", retryCount, product.toString()), e);
                    }
                }
                product.setCreated(true);
                productRepository.save(product);
            }

            if ((i+1) % 100 == 0) {
                log.info((i+1)+"/"+count+" products of product type '"+productType.getName()+"' processed");
            }
        }
    }
}
