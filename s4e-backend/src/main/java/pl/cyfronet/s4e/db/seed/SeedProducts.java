package pl.cyfronet.s4e.db.seed;

import lombok.Builder;
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

    @Value("${seed.products.seed-db:true}")
    private boolean seedDb;

    @Value("${seed.products.sync-geoserver:true}")
    private boolean syncGeoserver;

    @Value("${seed.products.sync-geoserver.reset-workspace:true}")
    private boolean syncGeoserverResetWorkspace;

    @Value("${seed.products.data-set:minio-data-v1}")
    private String dataSet;

    private final ProductTypeRepository productTypeRepository;
    private final ProductRepository productRepository;
    private final SldStyleRepository sldStyleRepository;
    private final PRGOverlayRepository prgOverlayRepository;

    private final GeoServerService geoServerService;
    private final GeoServerSynchronizer geoServerSynchronizer;

    @Async
    @Override
    public void run(ApplicationArguments args) {
        if (syncGeoserver && syncGeoserverResetWorkspace) {
            geoServerService.resetWorkspace();
        }

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
        switch (dataSet) {
            case "minio-data-v1":
                seedProductsMinioDataV1();
                break;
            case "s4e-demo":
                seedProductsS4EDemo();
                break;
            default:
                throw new IllegalStateException("Data set: '"+dataSet+"' not recognized");
        }
    }

    private void seedProductsMinioDataV1() {
        log.info("Seeding ProductTypes: minio-data-v1");
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

        log.info("Seeding Products, from: "+startInclusive.toString()+" to "+endExclusive.toString());
        for (val productType: productTypes) {
            seedProducts(productType, productParams.get(productType.getName()));
        }
    }

    private void seedProductsS4EDemo() {
        log.info("Seeding ProductTypes: s4e-demo");
        List<ProductType> productTypes = Arrays.asList(new ProductType[]{
                ProductType.builder()
                        .name("108m")
                        .description("Obraz satelitarny Meteosat dla obszaru Europy w kanale 10.8 µm z zastosowanie maskowanej palety barw dla obszarów mórz i lądów.")
                        .build(),
                ProductType.builder()
                        .name("NatCol")
                        .description("Opis produktu NatCol.")
                        .build(),
                ProductType.builder()
                        .name("Polsafi")
                        .description("Opis produktu Polsafi.")
                        .build(),
                ProductType.builder()
                        .name("RGB24_micro")
                        .description("Opis produktu RGB24_micro.")
                        .build(),
                ProductType.builder()
                        .name("Setvak")
                        .description("Obraz satelitarny Meteosat w kanale 10.8 µm z paletą barwną do analizy powierzchni wysokich chmur konwekcyjnych – obszar Europy Centralnej.")
                        .build(),
        });
        productTypeRepository.saveAll(productTypes);

        val productParams = Map.of(
                "108m", ProductParams.builder()
                        .startInclusive(LocalDateTime.of(2019,10,1,0,0))
                        .endExclusive(LocalDateTime.of(2019,11,1,0,0))
                        .layerNameFormat("108m_{timestamp}")
                        .s3PathFormat("MSG_Products_WM/108m/{date}/{timestamp}_kan_10800m.tif")
                        .build(),
                "NatCol", ProductParams.builder()
                        .startInclusive(LocalDateTime.of(2019,06,1,0,0))
                        .endExclusive(LocalDateTime.of(2019,07,1,0,0))
                        .layerNameFormat("NatCol_{timestamp}")
                        .s3PathFormat("MSG_Products_WM/NatCol/{date}/{timestamp}_RGB_Nat_Co.tif")
                        .build(),
                "Polsafi", ProductParams.builder()
                        .startInclusive(LocalDateTime.of(2019,9,1,0,0))
                        .endExclusive(LocalDateTime.of(2019,10,1,0,0))
                        .layerNameFormat("Polsafi_{timestamp}")
                        .s3PathFormat("MSG_Products_WM/Polsafi/{date}/{timestamp}_Polsaf.tif")
                        .build(),
                "RGB24_micro", ProductParams.builder()
                        .startInclusive(LocalDateTime.of(2019,8,1,0,0))
                        .endExclusive(LocalDateTime.of(2019,9,1,0,0))
                        .layerNameFormat("RGB24micro_{timestamp}")
                        .s3PathFormat("MSG_Products_WM/RGB24_micro/{date}/{timestamp}_RGB_24_micro.gif.tif")
                        .build(),
                "Setvak", ProductParams.builder()
                        .startInclusive(LocalDateTime.of(2019,7,1,0,0))
                        .endExclusive(LocalDateTime.of(2019,8,1,0,0))
                        .layerNameFormat("Setvak_{timestamp}")
                        .s3PathFormat("MSG_Products_WM/Setvak_Eu/{date}/{timestamp}_Eu_centr_ir_108_setvak_wtemp.tif")
                        .build()
        );

        for (val productType: productTypes) {
            seedProducts(productType, productParams.get(productType.getName()));
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
        val prgOverlays = List.of(new PRGOverlay[] {
                PRGOverlay.builder()
                        .name("wojewodztwa")
                        .featureType("wojew%C3%B3dztwa")
                        .sldStyle(sldStyles.get(0))
                        .build(),
                PRGOverlay.builder()
                        .name("powiaty")
                        .featureType("powiaty")
                        .sldStyle(sldStyles.get(0))
                        .build(),
                PRGOverlay.builder()
                        .name("gminy")
                        .featureType("gminy")
                        .sldStyle(sldStyles.get(0))
                        .build(),
                PRGOverlay.builder()
                        .name("jednostki_ewidencyjne")
                        .featureType("jednostki_ewidencyjne")
                        .sldStyle(sldStyles.get(0))
                        .build(),
                PRGOverlay.builder()
                        .name("obreby_ewidencyjne")
                        .featureType("obreby_ewidencyjne")
                        .sldStyle(sldStyles.get(0))
                        .build(),
        });
        prgOverlayRepository.saveAll(prgOverlays);
    }

    private void seedProducts(ProductType productType, ProductParams params) {
        val count = Duration.between(params.startInclusive, params.endExclusive).toHours();
        log.info("Seeding products of product type '"+productType.getName()+"', "+count+" total (from "+params.startInclusive+" to "+params.endExclusive+")");
        for (long i = 0; i < count; i++) {
            val timestamp = params.startInclusive.plusHours(i);
            Function<String, String> replacer = (str) -> str
                    .replace("{timestamp}", DATE_TIME_PATTERN.format(timestamp))
                    .replace("{date}", DATE_PATTERN.format(timestamp));
            val layerName = replacer.apply(params.layerNameFormat);
            val s3Path = replacer.apply(params.s3PathFormat);

            if (syncGeoserver) {
                val product = Product.builder()
                        .productType(productType)
                        .timestamp(timestamp)
                        .layerName(layerName)
                        .s3Path(s3Path)
                        .created(!syncGeoserver)
                        .build();

                productRepository.save(product);

                try {
                    if (!geoServerService.layerExists(product.getLayerName())) {
                        geoServerService.addLayer(product);
                    }
                    product.setCreated(true);
                    productRepository.save(product);
                } catch (Exception e) {
                    log.warn(String.format("Cannot create layer for %s. Deleting product", product.toString()), e);
                    try {
                        productRepository.delete(product);
                    } catch (Exception e1) {
                        // ignore
                    }
                }
            // If we don't sync GeoServer, verify the layer exists before saving.
            } else if (geoServerService.layerExists(layerName)) {
                val product = Product.builder()
                        .productType(productType)
                        .timestamp(timestamp)
                        .layerName(layerName)
                        .s3Path(s3Path)
                        .created(true)
                        .build();

                productRepository.save(product);
            } else {
                log.info(String.format("Layer '%s' doesn't exist, omitting productType '%s' timestamp %s", layerName, productType.getName(), timestamp));
            }

            if ((i+1) % 100 == 0) {
                log.info((i+1)+"/"+count+" products of product type '"+productType.getName()+"' processed");
            }
        }
    }
}
