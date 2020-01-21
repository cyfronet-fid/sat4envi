package pl.cyfronet.s4e.db.seed;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.bean.PRGOverlay;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.bean.SldStyle;
import pl.cyfronet.s4e.data.repository.PRGOverlayRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.data.repository.SldStyleRepository;
import pl.cyfronet.s4e.geoserver.sync.GeoServerSynchronizer;
import pl.cyfronet.s4e.properties.SeedProperties;
import pl.cyfronet.s4e.service.GeoServerService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Profile({"development & !skip-seed-products", "run-seed-products"})
@Component
@RequiredArgsConstructor
@Slf4j
public class SeedProducts implements ApplicationRunner {
    private static final DateTimeFormatter DATE_TIME_PATTERN = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Builder
    private static class ProductParams {
        private final LocalDateTime startInclusive;
        private final LocalDateTime endExclusive;
        private final String s3PathFormat;
        private final String layerNameFormat;
    }

    private final SeedProperties seedProperties;
    private final ProductRepository productRepository;
    private final SceneRepository sceneRepository;
    private final SldStyleRepository sldStyleRepository;
    private final PRGOverlayRepository prgOverlayRepository;

    private final GeoServerService geoServerService;
    private final GeoServerSynchronizer geoServerSynchronizer;

    @Async
    @Override
    public void run(ApplicationArguments args) {
        if (seedProperties.isSyncGeoserver() && seedProperties.isSyncGeoserverResetWorkspace()) {
            geoServerService.resetWorkspace();
        }

        if (seedProperties.isSeedDb()) {
            sceneRepository.deleteAll();
            productRepository.deleteAll();
            prgOverlayRepository.deleteAll();
            sldStyleRepository.deleteAll();

            seedScenes();
            seedOverlays();
        }

        if (seedProperties.isSyncGeoserver()) {
            try {
                geoServerSynchronizer.synchronizeOverlays();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }

        log.info("Seeding complete");
    }

    private void seedScenes() {
        switch (seedProperties.getDataSet()) {
            case "minio-data-v1":
                seedProductsMinioDataV1();
                break;
            case "s4e-demo":
                seedProductsS4EDemo();
                break;
            default:
                throw new IllegalStateException("Data set: '" + seedProperties.getDataSet() + "' not recognized");
        }
    }

    private void seedProductsMinioDataV1() {
        log.info("Seeding Products: minio-data-v1");
        List<Product> products = Arrays.asList(new Product[]{
                Product.builder()
                        .name("108m")
                        .description("Obraz satelitarny Meteosat dla obszaru Europy w kanale 10.8 µm z zastosowanie maskowanej palety barw dla obszarów mórz i lądów.")
                        .build(),
                Product.builder()
                        .name("Setvak")
                        .description("Obraz satelitarny Meteosat w kanale 10.8 µm z paletą barwną do analizy powierzchni wysokich chmur konwekcyjnych – obszar Europy Centralnej.")
                        .build(),
                Product.builder()
                        .name("WV-IR")
                        .description("Opis produktu WV-IR.")
                        .build(),
        });
        productRepository.saveAll(products);

        LocalDateTime startInclusive = LocalDateTime.of(2018, 10, 4, 0, 0);
        LocalDateTime endExclusive = startInclusive.plusDays(1);

        val productParams = Map.of(
                "108m", ProductParams.builder()
                        .startInclusive(startInclusive)
                        .endExclusive(endExclusive)
                        .layerNameFormat("108m_{timestamp}")
                        .s3PathFormat("{timestamp}_Merkator_Europa_ir_108m.tif")
                        .build(),
                "Setvak", ProductParams.builder()
                        .startInclusive(startInclusive)
                        .endExclusive(endExclusive)
                        .layerNameFormat("Setvak_{timestamp}")
                        .s3PathFormat("{timestamp}_Merkator_Europa_ir_108_setvak.tif")
                        .build(),
                "WV-IR", ProductParams.builder()
                        .startInclusive(startInclusive)
                        .endExclusive(endExclusive)
                        .layerNameFormat("WV-IR_{timestamp}")
                        .s3PathFormat("{timestamp}_Merkator_WV-IR.tif")
                        .build()
        );

        log.info("Seeding Scenes, from: " + startInclusive.toString() + " to " + endExclusive.toString());
        for (val product : products) {
            seedScenes(product, productParams.get(product.getName()));
        }
    }

    private void seedProductsS4EDemo() {
        log.info("Seeding Products: s4e-demo");
        List<Product> products = Arrays.asList(new Product[]{
                Product.builder() // 108m
                        .name("Zachmurzenie (108m)")
                        .description("Obraz satelitarny IR 10.8µm maskowany (różne palety barwne dla lądu, morza i chmur)")
                        .build(),
                Product.builder() // NatCol
                        .name("Detekcja chmur lodowych i śniegu")
                        .description("Kompozycja barwna RGB Natural Colors (dostępna tylko w ciągu dnia)")
                        .build(),
                Product.builder() // Polsafi
                        .name("Burze")
                        .description("Obraz satelitarny HRV z nałożonymi wyładowaniami atmosferycznymi (dostępny tylko w ciągu dnia)")
                        .build(),
                Product.builder() // RGB24_micro
                        .name("Mikrofizyka chmur")
                        .description("Kompozycja barwna RGB Mikrofizyka 24 godzinna do detekcji różnego typu zachmurzenia")
                        .build(),
                Product.builder() // Setvak_Eu
                        .name("Chmury konwekcyjne")
                        .description("Obraz satelitarny IR z dedykowaną paletą barwną")
                        .build(),
        });
        productRepository.saveAll(products);

        val productParams = Map.of(
                products.get(0).getName(), ProductParams.builder()
                        .startInclusive(LocalDateTime.of(2019, 10, 1, 0, 0))
                        .endExclusive(LocalDateTime.of(2019, 11, 1, 0, 0))
                        .layerNameFormat("108m_{timestamp}")
                        .s3PathFormat("MSG_Products_WM/108m/{date}/{timestamp}_kan_10800m.tif")
                        .build(),
                products.get(1).getName(), ProductParams.builder()
                        .startInclusive(LocalDateTime.of(2019, 06, 1, 0, 0))
                        .endExclusive(LocalDateTime.of(2019, 07, 1, 0, 0))
                        .layerNameFormat("NatCol_{timestamp}")
                        .s3PathFormat("MSG_Products_WM/NatCol/{date}/{timestamp}_RGB_Nat_Co.tif")
                        .build(),
                products.get(2).getName(), ProductParams.builder()
                        .startInclusive(LocalDateTime.of(2019, 9, 1, 0, 0))
                        .endExclusive(LocalDateTime.of(2019, 10, 1, 0, 0))
                        .layerNameFormat("Polsafi_{timestamp}")
                        .s3PathFormat("MSG_Products_WM/Polsafi/{date}/{timestamp}_Polsaf.tif")
                        .build(),
                products.get(3).getName(), ProductParams.builder()
                        .startInclusive(LocalDateTime.of(2019, 8, 1, 0, 0))
                        .endExclusive(LocalDateTime.of(2019, 9, 1, 0, 0))
                        .layerNameFormat("RGB24micro_{timestamp}")
                        .s3PathFormat("MSG_Products_WM/RGB24_micro/{date}/{timestamp}_RGB_24_micro.gif.tif")
                        .build(),
                products.get(4).getName(), ProductParams.builder()
                        .startInclusive(LocalDateTime.of(2019, 7, 1, 0, 0))
                        .endExclusive(LocalDateTime.of(2019, 8, 1, 0, 0))
                        .layerNameFormat("Setvak_{timestamp}")
                        .s3PathFormat("MSG_Products_WM/Setvak_Eu/{date}/{timestamp}_Eu_centr_ir_108_setvak_wtemp.tif")
                        .build()
        );

        for (val product : products) {
            seedScenes(product, productParams.get(product.getName()));
        }
    }

    private void seedOverlays() {
        log.info("Seeding SldStyles");
        val sldStyles = new ArrayList<SldStyle>();
        sldStyles.add(SldStyle.builder()
                .name("wojewodztwa")
                .build());
        sldStyleRepository.saveAll(sldStyles);

        log.info("Seeding PRGOverlays");
        val prgOverlays = List.of(new PRGOverlay[]{
                PRGOverlay.builder()
                        .name("wojewodztwa")
                        .featureType("wojewodztwa")
                        .build(),
                PRGOverlay.builder()
                        .name("powiaty")
                        .featureType("powiaty")
                        .build(),
                PRGOverlay.builder()
                        .name("gminy")
                        .featureType("gminy")
                        .build(),
                PRGOverlay.builder()
                        .name("jednostki_ewidencyjne")
                        .featureType("jednostki_ewidencyjne")
                        .build(),
                PRGOverlay.builder()
                        .name("obreby_ewidencyjne")
                        .featureType("obreby_ewidencyjne")
                        .build(),
        });
        prgOverlays.forEach(overlay -> {
            overlay.setCreated(!seedProperties.isSyncGeoserver());
            overlay.setSldStyle(sldStyles.get(0));
        });
        prgOverlayRepository.saveAll(prgOverlays);
    }

    private void seedScenes(Product product, ProductParams params) {
        val count = Duration.between(params.startInclusive, params.endExclusive).toHours();
        log.info("Seeding scenes of product '" + product.getName() + "', " + count + " total (from " + params.startInclusive + " to " + params.endExclusive + ")");
        for (long i = 0; i < count; i++) {
            val timestamp = params.startInclusive.plusHours(i);
            Function<String, String> replacer = (str) -> str
                    .replace("{timestamp}", DATE_TIME_PATTERN.format(timestamp))
                    .replace("{date}", DATE_PATTERN.format(timestamp));
            val layerName = replacer.apply(params.layerNameFormat);
            val s3Path = replacer.apply(params.s3PathFormat);

            if (seedProperties.isSyncGeoserver()) {
                val scene = Scene.builder()
                        .product(product)
                        .timestamp(timestamp)
                        .layerName(layerName)
                        .s3Path(s3Path)
                        .created(!seedProperties.isSyncGeoserver())
                        .build();

                sceneRepository.save(scene);

                try {
                    if (!geoServerService.layerExists(scene.getLayerName())) {
                        geoServerService.addLayer(scene);
                    }
                    scene.setCreated(true);
                    sceneRepository.save(scene);
                } catch (Exception e) {
                    log.warn(String.format("Cannot create layer for %s. Deleting scene", scene.toString()), e);
                    try {
                        sceneRepository.delete(scene);
                    } catch (Exception e1) {
                        // ignore
                    }
                }
            // If we don't sync GeoServer, verify the layer exists before saving.
            } else if (geoServerService.layerExists(layerName)) {
                val scene = Scene.builder()
                        .product(product)
                        .timestamp(timestamp)
                        .layerName(layerName)
                        .s3Path(s3Path)
                        .created(true)
                        .build();

                sceneRepository.save(scene);
            } else {
                log.info(String.format("Layer '%s' doesn't exist, omitting product '%s' timestamp %s", layerName, product.getName(), timestamp));
            }

            if ((i + 1) % 100 == 0) {
                log.info((i + 1) + "/" + count + " scenes of product '" + product.getName() + "' processed");
            }
        }
    }
}
