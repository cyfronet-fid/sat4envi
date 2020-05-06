package pl.cyfronet.s4e.db.seed;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.data.repository.*;
import pl.cyfronet.s4e.ex.S3ClientException;
import pl.cyfronet.s4e.geoserver.sync.GeoServerSynchronizer;
import pl.cyfronet.s4e.properties.GeoServerProperties;
import pl.cyfronet.s4e.properties.S3Properties;
import pl.cyfronet.s4e.properties.SeedProperties;
import pl.cyfronet.s4e.service.GeoServerService;
import pl.cyfronet.s4e.service.SceneStorage;
import pl.cyfronet.s4e.sync.PrefixScanner;
import pl.cyfronet.s4e.sync.SceneAcceptor;
import pl.cyfronet.s4e.util.GeometryUtil;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Profile({"development & !skip-seed-products", "run-seed-products"})
@Component
@RequiredArgsConstructor
@Slf4j
public class SeedProducts implements ApplicationRunner {
    private static final DateTimeFormatter DATE_TIME_PATTERN = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter YEAR_PATTERN = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter TIME_PATTERN = DateTimeFormatter.ofPattern("HHmm");

    @Builder
    @Value
    private static class ProductParams {
        LocalDateTime startInclusive;
        LocalDateTime endExclusive;
        String s3PathFormat;
        Geometry footprint;
        @Builder.Default Duration increment = Duration.ofHours(1);
    }

    @Builder
    @Value
    private static class ProductParamsPair {
        Product product;
        ProductParams params;
    }

    private final SeedProperties seedProperties;
    private final S3Properties s3Properties;
    private final GeoServerProperties geoServerProperties;

    private final SchemaRepository schemaRepository;
    private final ProductRepository productRepository;
    private final SceneRepository sceneRepository;
    private final SldStyleRepository sldStyleRepository;
    private final PRGOverlayRepository prgOverlayRepository;

    private final GeoServerService geoServerService;
    private final GeoServerSynchronizer geoServerSynchronizer;
    private final SceneStorage sceneStorage;

    private final GeometryUtil geom;

    private final JdbcTemplate jdbcTemplate;

    private final PrefixScanner prefixScanner;
    private final SceneAcceptor sceneAcceptor;
    private final SchemaScanner schemaScanner;

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
            schemaRepository.deleteAll();

            seedScenes();
            seedOverlays();
        }

        if (seedProperties.isSyncGeoserver()) {
            try {
                geoServerSynchronizer.synchronizeStoreAndMosaics();
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
            case "s4e-demo-2":
                seedProductsS4EDemo2();
                break;
            case "s4e-sync-1":
                seedProductsSync1();
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
                        .displayName("108m")
                        .description("Obraz satelitarny Meteosat dla obszaru Europy w kanale 10.8 µm z zastosowanie maskowanej palety barw dla obszarów mórz i lądów.")
                        .layerName("108m")
                        .build(),
                Product.builder()
                        .name("Setvak")
                        .displayName("Setvak")
                        .description("Obraz satelitarny Meteosat w kanale 10.8 µm z paletą barwną do analizy powierzchni wysokich chmur konwekcyjnych – obszar Europy Centralnej.")
                        .layerName("setvak")
                        .build(),
                Product.builder()
                        .name("WV_IR")
                        .displayName("WV-IR")
                        .description("Opis produktu WV-IR.")
                        .layerName("wv_ir")
                        .build(),
        });
        productRepository.saveAll(products);
        createViews(products);

        LocalDateTime startInclusive = LocalDateTime.of(2018, 10, 4, 0, 0);
        LocalDateTime endExclusive = startInclusive.plusDays(1);

        String minX = "-4114278.408460264";
        String minY = "2152803.882602471";
        String maxX = "6349395.119539737";
        String maxY = "12559354.462885914";
        String a1 = minX+" "+minY;
        String a2 = minX+" "+maxY;
        String a3 = maxX+" "+maxY;
        String a4 = maxX+" "+minY;

        Geometry footprint;
        try {
            footprint = geom.parseWKT("POLYGON(("+a1+","+a2+","+a3+","+a4+","+a1+"))", GeometryUtil.FACTORY_3857);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }

        val productParams = Map.of(
                "108m", ProductParams.builder()
                        .startInclusive(startInclusive)
                        .endExclusive(endExclusive)
                        .s3PathFormat("{timestamp}_Merkator_Europa_ir_108m.tif")
                        .footprint(footprint)
                        .build(),
                "Setvak", ProductParams.builder()
                        .startInclusive(startInclusive)
                        .endExclusive(endExclusive)
                        .s3PathFormat("{timestamp}_Merkator_Europa_ir_108_setvak.tif")
                        .footprint(footprint)
                        .build(),
                "WV_IR", ProductParams.builder()
                        .startInclusive(startInclusive)
                        .endExclusive(endExclusive)
                        .s3PathFormat("{timestamp}_Merkator_WV-IR.tif")
                        .footprint(footprint)
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
                Product.builder()
                        .name("108m")
                        .displayName("Zachmurzenie (108m)")
                        .description("Obraz satelitarny IR 10.8µm maskowany (różne palety barwne dla lądu, morza i chmur)")
                        .layerName("108m")
                        .build(),
                Product.builder()
                        .name("NatCol")
                        .displayName("Detekcja chmur lodowych i śniegu")
                        .description("Kompozycja barwna RGB Natural Colors (dostępna tylko w ciągu dnia)")
                        .layerName("natcol")
                        .build(),
                Product.builder()
                        .name("Polsafi")
                        .displayName("Burze")
                        .description("Obraz satelitarny HRV z nałożonymi wyładowaniami atmosferycznymi (dostępny tylko w ciągu dnia)")
                        .layerName("polsafi")
                        .build(),
                Product.builder()
                        .name("RGB24_micro")
                        .displayName("Mikrofizyka chmur")
                        .description("Kompozycja barwna RGB Mikrofizyka 24 godzinna do detekcji różnego typu zachmurzenia")
                        .layerName("rgb24_micro")
                        .build(),
                Product.builder()
                        .name("Setvak_Eu")
                        .displayName("Chmury konwekcyjne")
                        .description("Obraz satelitarny IR z dedykowaną paletą barwną")
                        .layerName("setvak_eu")
                        .build(),
        });
        productRepository.saveAll(products);
        createViews(products);

        try {
            val productParams = Map.of(
                    products.get(0).getName(), ProductParams.builder()
                            .startInclusive(LocalDateTime.of(2019, 10, 1, 0, 0))
                            .endExclusive(LocalDateTime.of(2019, 11, 1, 0, 0))
                            .s3PathFormat("MSG_Products_WM/108m/{date}/{timestamp}_kan_10800m.tif")
                            .footprint(geom.parseWKT("POLYGON((-5873698.67467749 2651116.00239174,-5873698.67467749 13108846.6493595,8837890.82944027 13108846.6493595,8837890.82944027 2651116.00239174,-5873698.67467749 2651116.00239174))", GeometryUtil.FACTORY_3857))
                            .build(),
                    products.get(1).getName(), ProductParams.builder()
                            .startInclusive(LocalDateTime.of(2019, 06, 1, 0, 0))
                            .endExclusive(LocalDateTime.of(2019, 07, 1, 0, 0))
                            .s3PathFormat("MSG_Products_WM/NatCol/{date}/{timestamp}_RGB_Nat_Co.tif")
                            .footprint(geom.parseWKT("POLYGON((-5873698.67467749 2651116.00239174,-5873698.67467749 13108846.6493595,8837890.82944027 13108846.6493595,8837890.82944027 2651116.00239174,-5873698.67467749 2651116.00239174))", GeometryUtil.FACTORY_3857))
                            .build(),
                    products.get(2).getName(), ProductParams.builder()
                            .startInclusive(LocalDateTime.of(2019, 9, 1, 0, 0))
                            .endExclusive(LocalDateTime.of(2019, 10, 1, 0, 0))
                            .s3PathFormat("MSG_Products_WM/Polsafi/{date}/{timestamp}_Polsaf.tif")
                            .footprint(geom.parseWKT("POLYGON((1400382.79507599 6018960.88304283,1400382.79507599 7411723.12812701,2905828.06969773 7411723.12812701,2905828.06969773 6018960.88304283,1400382.79507599 6018960.88304283))", GeometryUtil.FACTORY_3857))
                            .build(),
                    products.get(3).getName(), ProductParams.builder()
                            .startInclusive(LocalDateTime.of(2019, 8, 1, 0, 0))
                            .endExclusive(LocalDateTime.of(2019, 9, 1, 0, 0))
                            .s3PathFormat("MSG_Products_WM/RGB24_micro/{date}/{timestamp}_RGB_24_micro.gif.tif")
                            .footprint(geom.parseWKT("POLYGON((-88492.2868752733 5025123.1874692,-88492.2868752733 8834823.91466135,4607643.45957048 8834823.91466135,4607643.45957048 5025123.1874692,-88492.2868752733 5025123.1874692))", GeometryUtil.FACTORY_3857))
                            .build(),
                    products.get(4).getName(), ProductParams.builder()
                            .startInclusive(LocalDateTime.of(2019, 7, 1, 0, 0))
                            .endExclusive(LocalDateTime.of(2019, 8, 1, 0, 0))
                            .s3PathFormat("MSG_Products_WM/Setvak_Eu/{date}/{timestamp}_Eu_centr_ir_108_setvak_wtemp.tif")
                            .footprint(geom.parseWKT("POLYGON((-88492.2868752733 5025123.1874692,-88492.2868752733 8834823.91466135,4607643.45957048 8834823.91466135,4607643.45957048 5025123.1874692,-88492.2868752733 5025123.1874692))", GeometryUtil.FACTORY_3857))
                            .build()
            );

            for (val product : products) {
                seedScenes(product, productParams.get(product.getName()));
            }
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    private void seedProductsS4EDemo2() {
        log.info("Seeding Products: s4e-demo-2");
        List<ProductParamsPair> prods = new ArrayList<>();
        try {
            prods.add(ProductParamsPair.builder()
                    .product(Product.builder()
                            .name("H03")
                            .displayName("Intensywność opadu")
                            .description("Produkt generowany na podstawie danych SEVIRI/METEOSAT oraz danych z czujników mikrofalowych satelitów okołobiegunowych. Przedstawia  intensywność opadu w mm/h.")
                            .layerName("h03")
                            .build())
                    .params(ProductParams.builder()
                            .startInclusive(LocalDateTime.of(2020, 1, 1, 0, 0))
                            .endExclusive(LocalDateTime.of(2020, 2, 1, 0, 0))
                            .s3PathFormat("MSG_Products_WM/Opad_H03/{date}/H3_{date}_{time}.tif")
                            .footprint(geom.parseWKT("POLYGON((1335833.88951928 6107041.03216704,1335833.88951928 7558415.65608179,2894508.58290632 7558415.65608179,2894508.58290632 6107041.03216704,1335833.88951928 6107041.03216704))", GeometryUtil.FACTORY_3857))
                            .build())
                    .build());
            prods.add(ProductParamsPair.builder()
                    .product(Product.builder()
                            .name("H05_03")
                            .displayName("Suma opadu (3h)")
                            .description("Produkt generowany na podstawie danych SEVIRI/METEOSAT oraz danych z czujników mikrofalowych satelitów okołobiegunowych. Obliczany na podstawie produktu „Intensywność opadu”.")
                            .layerName("h05_03")
                            .build())
                    .params(ProductParams.builder()
                            .startInclusive(LocalDateTime.of(2019, 7, 2, 0, 0))
                            .endExclusive(LocalDateTime.of(2019, 8, 1, 0, 0))
                            .increment(Duration.ofHours(3))
                            .s3PathFormat("MSG_Products_WM/Opad_H05/{date}/H5_{date}_{time}_03.tif")
                            .footprint(geom.parseWKT("POLYGON((1447153.38031256 6274987.3523514,1447153.38031256 7361866.11305119,2783679.36335795 7361866.11305119,2783679.36335795 6274987.3523514,1447153.38031256 6274987.3523514))", GeometryUtil.FACTORY_3857))
                            .build())
                    .build());
            prods.add(ProductParamsPair.builder()
                    .product(Product.builder()
                            .name("H05_06")
                            .displayName("Suma opadu (6h)")
                            .description("Produkt generowany na podstawie danych SEVIRI/METEOSAT oraz danych z czujników mikrofalowych satelitów okołobiegunowych. Obliczany na podstawie produktu „Intensywność opadu”.")
                            .layerName("h05_06")
                            .build())
                    .params(ProductParams.builder()
                            .startInclusive(LocalDateTime.of(2019, 7, 2, 0, 0))
                            .endExclusive(LocalDateTime.of(2019, 8, 1, 0, 0))
                            .increment(Duration.ofHours(3))
                            .s3PathFormat("MSG_Products_WM/Opad_H05/{date}/H5_{date}_{time}_06.tif")
                            .footprint(geom.parseWKT("POLYGON((1447153.38031256 6274987.3523514,1447153.38031256 7361866.11305119,2783679.36335795 7361866.11305119,2783679.36335795 6274987.3523514,1447153.38031256 6274987.3523514))", GeometryUtil.FACTORY_3857))
                            .build())
                    .build());
            prods.add(ProductParamsPair.builder()
                    .product(Product.builder()
                            .name("H05_12")
                            .displayName("Suma opadu (12h)")
                            .description("Produkt generowany na podstawie danych SEVIRI/METEOSAT oraz danych z czujników mikrofalowych satelitów okołobiegunowych. Obliczany na podstawie produktu „Intensywność opadu”.")
                            .layerName("h05_12")
                            .build())
                    .params(ProductParams.builder()
                            .startInclusive(LocalDateTime.of(2019, 7, 2, 0, 0))
                            .endExclusive(LocalDateTime.of(2019, 8, 1, 0, 0))
                            .increment(Duration.ofHours(3))
                            .s3PathFormat("MSG_Products_WM/Opad_H05/{date}/H5_{date}_{time}_12.tif")
                            .footprint(geom.parseWKT("POLYGON((1447153.38031256 6274987.3523514,1447153.38031256 7361866.11305119,2783679.36335795 7361866.11305119,2783679.36335795 6274987.3523514,1447153.38031256 6274987.3523514))", GeometryUtil.FACTORY_3857))
                            .build())
                    .build());
            prods.add(ProductParamsPair.builder()
                    .product(Product.builder()
                            .name("H05_24")
                            .displayName("Suma opadu (24h)")
                            .description("Produkt generowany na podstawie danych SEVIRI/METEOSAT oraz danych z czujników mikrofalowych satelitów okołobiegunowych. Obliczany na podstawie produktu „Intensywność opadu”.")
                            .layerName("h05_24")
                            .build())
                    .params(ProductParams.builder()
                            .startInclusive(LocalDateTime.of(2019, 7, 2, 0, 0))
                            .endExclusive(LocalDateTime.of(2019, 8, 1, 0, 0))
                            .increment(Duration.ofHours(3))
                            .s3PathFormat("MSG_Products_WM/Opad_H05/{date}/H5_{date}_{time}_24.tif")
                            .footprint(geom.parseWKT("POLYGON((1447153.38031256 6274987.3523514,1447153.38031256 7361866.11305119,2783679.36335795 7361866.11305119,2783679.36335795 6274987.3523514,1447153.38031256 6274987.3523514))", GeometryUtil.FACTORY_3857))
                            .build())
                    .build());
            prods.add(ProductParamsPair.builder()
                    .product(Product.builder()
                            .name("OST")
                            .displayName("Chmury konwekcyjne wysoko wypiętrzone (Overshooting Tops)")
                            .description("Przetworzony obraz Meteosat dla obszaru Polski – różnica kanałów 6.2 i 10.8 µm, do identyfikacji wysoko wypiętrzonych chmur konwekcyjnych (Overshooting Tops).")
                            .layerName("ost")
                            .build())
                    .params(ProductParams.builder()
                            .startInclusive(LocalDateTime.of(2019, 7, 1, 0, 0))
                            .endExclusive(LocalDateTime.of(2019, 8, 1, 0, 0))
                            .s3PathFormat("MSG_Products_WM/OST/{date}/{timestamp}_WV-IR.tif")
                            .footprint(geom.parseWKT("POLYGON((1400382.79507599 6018960.88304283,1400382.79507599 7411723.12812701,2905828.06969773 7411723.12812701,2905828.06969773 6018960.88304283,1400382.79507599 6018960.88304283))", GeometryUtil.FACTORY_3857))
                            .build())
                    .build());
            prods.add(ProductParamsPair.builder()
                    .product(Product.builder() // s4e-demo 3
                            .name("Polsafi")
                            .displayName("Polsafi, wyładowania atmosferyczne")
                            .description("Obraz satelitarny Meteosat dla obszaru Polski w kanale HRV (0.4-1.1 µm) z nałożonymi wyładowaniami atmosferycznymi (dostępny tylko w ciągu dnia)")
                            .layerName("polsafi")
                            .build())
                    .params(ProductParams.builder()
                            .startInclusive(LocalDateTime.of(2019, 9, 1, 0, 0))
                            .endExclusive(LocalDateTime.of(2019, 10, 1, 0, 0))
                            .s3PathFormat("MSG_Products_WM/Polsafi/{date}/{timestamp}_Polsaf.tif")
                            .footprint(geom.parseWKT("POLYGON((1400382.79507599 6018960.88304283,1400382.79507599 7411723.12812701,2905828.06969773 7411723.12812701,2905828.06969773 6018960.88304283,1400382.79507599 6018960.88304283))", GeometryUtil.FACTORY_3857))
                            .build())
                    .build());
            prods.add(ProductParamsPair.builder()
                    .product(Product.builder()
                            .name("SM1")
                            .displayName("Wilgotność gleby - SM1")
                            .description("Procentowy wskaźnik wilgotności gleby - SM1")
                            .layerName("sm1")
                            .build())
                    .params(ProductParams.builder()
                            .startInclusive(LocalDateTime.of(2020, 1, 1, 0, 0))
                            .endExclusive(LocalDateTime.of(2020, 2, 3, 0, 0))
                            .increment(Duration.ofDays(1))
                            .s3PathFormat("MSG_Products_WM/Soil_Moisture/SM1/{year}/SM1_{date}_WM.tif")
                            .footprint(geom.parseWKT("POLYGON((1558472.87110583 6275378.93937109,1558472.87110583 7361866.11305119,2693587.98734195 7361866.11305119,2693587.98734195 6275378.93937109,1558472.87110583 6275378.93937109))", GeometryUtil.FACTORY_3857))
                            .build())
                    .build());
            prods.add(ProductParamsPair.builder()
                    .product(Product.builder()
                            .name("SM2")
                            .displayName("Wilgotność gleby - SM2")
                            .description("Procentowy wskaźnik wilgotności gleby - SM2")
                            .layerName("sm2")
                            .build())
                    .params(ProductParams.builder()
                            .startInclusive(LocalDateTime.of(2019, 1, 2, 0, 0))
                            .endExclusive(LocalDateTime.of(2020, 1, 17, 0, 0))
                            .increment(Duration.ofDays(1))
                            .s3PathFormat("MSG_Products_WM/Soil_Moisture/SM2/{year}/SM2_{date}_WM.tif")
                            .footprint(geom.parseWKT("POLYGON((1558472.87110583 6275378.93937109,1558472.87110583 7361866.11305119,2693587.98734195 7361866.11305119,2693587.98734195 6275378.93937109,1558472.87110583 6275378.93937109))", GeometryUtil.FACTORY_3857))
                            .build())
                    .build());
            prods.add(ProductParamsPair.builder()
                    .product(Product.builder()
                            .name("Dust")
                            .displayName("Pył w atmosferze")
                            .description("Obraz satelitarny Meteosat dla obszaru Europy, kompozycja RBG Dust (6.2-7.3/3.9-10.8/1.6-0.6) do identyfikacji wysokiej koncentracji pyłu w atmosferze.")
                            .layerName("dust")
                            .build())
                    .params(ProductParams.builder()
                            .startInclusive(LocalDateTime.of(2020, 1, 1, 0, 0))
                            .endExclusive(LocalDateTime.of(2020, 2, 1, 0, 0))
                            .s3PathFormat("MSG_Products_WM/Dust/{date}/{timestamp}_RGB_DUST_Eu.tif")
                            .footprint(geom.parseWKT("POLYGON((-5873698.67467749 2651116.00239174,-5873698.67467749 13108846.6493595,8837890.82944027 13108846.6493595,8837890.82944027 2651116.00239174,-5873698.67467749 2651116.00239174))", GeometryUtil.FACTORY_3857))
                            .build())
                    .build());
            prods.add(ProductParamsPair.builder()
                    .product(Product.builder() // s4e-demo 2
                            .name("NatCol")
                            .displayName("Detekcja chmur lodowych i śniegu")
                            .description("Kompozycja barwna RGB Natural Colors (dostępna tylko w ciągu dnia)")
                            .layerName("natcol")
                            .build())
                    .params(ProductParams.builder()
                            .startInclusive(LocalDateTime.of(2019, 06, 1, 0, 0))
                            .endExclusive(LocalDateTime.of(2019, 07, 1, 0, 0))
                            .s3PathFormat("MSG_Products_WM/NatCol/{date}/{timestamp}_RGB_Nat_Co.tif")
                            .footprint(geom.parseWKT("POLYGON((-5873698.67467749 2651116.00239174,-5873698.67467749 13108846.6493595,8837890.82944027 13108846.6493595,8837890.82944027 2651116.00239174,-5873698.67467749 2651116.00239174))", GeometryUtil.FACTORY_3857))
                            .build())
                    .build());
            prods.add(ProductParamsPair.builder()
                    .product(Product.builder() // s4e-demo 1
                            .name("108m")
                            .displayName("Zachmurzenie (108m)")
                            .description("Obraz satelitarny IR 10.8µm maskowany (różne palety barwne dla lądu, morza i chmur)")
                            .layerName("108m")
                            .build())
                    .params(ProductParams.builder()
                            .startInclusive(LocalDateTime.of(2019, 10, 1, 0, 0))
                            .endExclusive(LocalDateTime.of(2019, 11, 1, 0, 0))
                            .s3PathFormat("MSG_Products_WM/108m/{date}/{timestamp}_kan_10800m.tif")
                            .footprint(geom.parseWKT("POLYGON((-5873698.67467749 2651116.00239174,-5873698.67467749 13108846.6493595,8837890.82944027 13108846.6493595,8837890.82944027 2651116.00239174,-5873698.67467749 2651116.00239174))", GeometryUtil.FACTORY_3857))
                            .build())
                    .build());
            prods.add(ProductParamsPair.builder()
                    .product(Product.builder() // s4e-demo 4
                            .name("RGB24_micro")
                            .displayName("Mikrofizyka chmur")
                            .description("Kompozycja barwna RGB Mikrofizyka 24 godzinna do detekcji różnego typu zachmurzenia")
                            .layerName("rgb24_micro")
                            .build())
                    .params(ProductParams.builder()
                            .startInclusive(LocalDateTime.of(2019, 8, 1, 0, 0))
                            .endExclusive(LocalDateTime.of(2019, 9, 1, 0, 0))
                            .s3PathFormat("MSG_Products_WM/RGB24_micro/{date}/{timestamp}_RGB_24_micro.gif.tif")
                            .footprint(geom.parseWKT("POLYGON((-88492.2868752733 5025123.1874692,-88492.2868752733 8834823.91466135,4607643.45957048 8834823.91466135,4607643.45957048 5025123.1874692,-88492.2868752733 5025123.1874692))", GeometryUtil.FACTORY_3857))
                            .build())
                    .build());
            prods.add(ProductParamsPair.builder()
                    .product(Product.builder() // s4e-demo 5
                            .name("Setvak_Eu")
                            .displayName("Chmury konwekcyjne")
                            .description("Obraz satelitarny IR z dedykowaną paletą barwną")
                            .layerName("setvak_eu")
                            .build())
                    .params(ProductParams.builder()
                            .startInclusive(LocalDateTime.of(2019, 7, 1, 0, 0))
                            .endExclusive(LocalDateTime.of(2019, 8, 1, 0, 0))
                            .s3PathFormat("MSG_Products_WM/Setvak_Eu/{date}/{timestamp}_Eu_centr_ir_108_setvak_wtemp.tif")
                            .footprint(geom.parseWKT("POLYGON((-88492.2868752733 5025123.1874692,-88492.2868752733 8834823.91466135,4607643.45957048 8834823.91466135,4607643.45957048 5025123.1874692,-88492.2868752733 5025123.1874692))", GeometryUtil.FACTORY_3857))
                            .build())
                    .build());

            prods.stream()
                    .map(ProductParamsPair::getProduct)
                    .forEach(productRepository::save);
            createViews(prods.stream().map(ProductParamsPair::getProduct).collect(Collectors.toList()));

            for (val pair : prods) {
                seedScenes(pair.getProduct(), pair.getParams());
            }
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    private void seedProductsSync1() {
        log.info("Seeding Products: s4e-sync-1");
        List<Schema> schemasList;
        try {
            schemasList = schemaScanner.scan("classpath:schema/s4e-sync-1");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        schemaRepository.saveAll(schemasList);
        Map<String, Schema> schemas = schemasList.stream().collect(Collectors.toMap(Schema::getName, s -> s));
        val granuleArtifactRuleMSG = Map.of("default", "product_file");
        val granuleArtifactRuleSentinel = Map.of(
                "default", "quicklook",
                "COG", "RGBs_8b"
        );
        List<Product> products = List.of(
                Product.builder()
                        .name("Setvak_Eu")
                        .layerName("setvak_eu")
                        .displayName("Chmury konwekcyjne")
                        .description("Obraz satelitarny IR z dedykowaną paletą barwną")
                        .sceneSchema(schemas.get("MSG.scene.v1.json"))
                        .metadataSchema(schemas.get("MSG.metadata.v1.json"))
                        .granuleArtifactRule(granuleArtifactRuleMSG)
                        .build(),
                Product.builder()
                        .name("Setvak_PL")
                        .layerName("setvak_pl")
                        .displayName("Chmury konwekcyjne (PL)")
                        .description("Obraz satelitarny IR z dedykowaną paletą barwną")
                        .sceneSchema(schemas.get("MSG.scene.v1.json"))
                        .metadataSchema(schemas.get("MSG.metadata.v1.json"))
                        .granuleArtifactRule(granuleArtifactRuleMSG)
                        .build(),
                Product.builder()
                        .name("OST")
                        .layerName("ost")
                        .displayName("Chmury konwekcyjne wysoko wypiętrzone (Overshooting Tops)")
                        .description("Przetworzony obraz Meteosat dla obszaru Polski – różnica kanałów 6.2 i 10.8 µm, do identyfikacji wysoko wypiętrzonych chmur konwekcyjnych (Overshooting Tops).")
                        .sceneSchema(schemas.get("MSG.scene.v1.json"))
                        .metadataSchema(schemas.get("MSG.metadata.v1.json"))
                        .granuleArtifactRule(granuleArtifactRuleMSG)
                        .build(),
                Product.builder()
                        .name("Sentinel-1-GRDH")
                        .layerName("sentinel_1_grdh")
                        .displayName("Sentinel 1 GRDH")
                        .description("Opis GRDH")
                        .sceneSchema(schemas.get("Sentinel-1.scene.v1.json"))
                        .metadataSchema(schemas.get("Sentinel-1.metadata.v1.json"))
                        .granuleArtifactRule(granuleArtifactRuleSentinel)
                        .build(),
                Product.builder()
                        .name("Sentinel-1-GRDM")
                        .layerName("sentinel_1_grdm")
                        .displayName("Sentinel 1 GRDM")
                        .description("Opis GRDM")
                        .sceneSchema(schemas.get("Sentinel-1.scene.v1.json"))
                        .metadataSchema(schemas.get("Sentinel-1.metadata.v1.json"))
                        .granuleArtifactRule(granuleArtifactRuleSentinel)
                        .build(),
                Product.builder()
                        .name("Sentinel-1-SLC_")
                        .layerName("sentinel_1_slc")
                        .displayName("Sentinel 1 SLC")
                        .description("Opis SLC")
                        .sceneSchema(schemas.get("Sentinel-1.scene.v1.json"))
                        .metadataSchema(schemas.get("Sentinel-1.metadata.v1.json"))
                        .granuleArtifactRule(granuleArtifactRuleSentinel)
                        .build()
        );

        productRepository.saveAll(products);
        createViews(products);

        Stream.of(
                "Sentinel-1/GRDH/",
                "Sentinel-1/GRDM/",
                "Sentinel-1/SLC_/",
                "MSG_Products_WM/Setvak_Eu/",
                "MSG_Products_WM/Setvak_PL/",
                "MSG_Products_WM/OST/"
        ).forEach(this::readScenes);
    }

    private void createViews(List<Product> products) {
        for (val product: products) {
            String id = product.getId().toString();
            String name = product.getLayerName();
            jdbcTemplate.execute("DROP VIEW IF EXISTS scene_" + name);
            jdbcTemplate.execute("CREATE VIEW scene_" + name + " AS " +
                    "SELECT  s.id, s.footprint, s.timestamp, s.granule_path " +
                    "FROM scene s " +
                    "WHERE s.product_id = " + id);
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
        val count = Duration.between(params.startInclusive, params.endExclusive).dividedBy(params.increment);
        log.info("Seeding scenes of product '" + product.getName() + "', " + count + " total (from " + params.startInclusive + " to " + params.endExclusive + ")");
        for (long i = 0; i < count; i++) {
            val timestamp = params.startInclusive.plus(params.increment.multipliedBy(i));
            Function<String, String> replacer = (str) -> str
                    .replace("{timestamp}", DATE_TIME_PATTERN.format(timestamp))
                    .replace("{date}", DATE_PATTERN.format(timestamp))
                    .replace("{year}", YEAR_PATTERN.format(timestamp))
                    .replace("{time}", TIME_PATTERN.format(timestamp));
            val s3Path = replacer.apply(params.s3PathFormat);
            val granulePath = geoServerProperties.getEndpoint() + "://" + s3Properties.getBucket() + "/" + s3Path;

            val scene = Scene.builder()
                    .product(product)
                    .timestamp(timestamp)
                    .s3Path(s3Path)
                    .granulePath(granulePath)
                    .footprint(params.getFootprint())
                    .build();

            try {
                if (sceneStorage.exists(s3Path)) {
                    sceneRepository.save(scene);
                } else {
                    log.info("Key doesn't exist: '" + s3Path + "', omitting scene");
                }
            } catch (S3ClientException e) {
                log.info("Aborting, exception when checking for existence of key: '" + s3Path + "'", e);
                return;
            }

            if ((i + 1) % 100 == 0) {
                log.info((i + 1) + "/" + count + " scenes of product '" + product.getName() + "' processed");
            }
        }
    }

    private void readScenes(String prefix) {
        // AWS SDK has a default connection pool size of 50, so 40 threads should be fine.
        ExecutorService es = Executors.newFixedThreadPool(40);

        try {
            List<String> allSceneKeys = prefixScanner.scan(prefix)
                    .map(S3Object::key)
                    .filter(key -> key.endsWith(".scene"))
                    .collect(Collectors.toList());
            log.info(String.format("Scanning prefix: '%s'. Total: %d", prefix, allSceneKeys.size()));
            List<String> sceneKeysToSync = optionallyTruncateToLimit(allSceneKeys, seedProperties.getS4eSyncV1().getLimit());
            AtomicInteger count = new AtomicInteger(0);
            es.submit(() -> sceneKeysToSync.stream()
                    .parallel()
                    .forEach(sceneKey -> {
                        sceneAcceptor.accept(sceneKey);
                        int i = count.addAndGet(1);
                        log.info(String.format("%d/%d. scene key: '%s'", i, sceneKeysToSync.size(), sceneKey));
                    })).get();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } catch (ExecutionException e) {
            log.error(e.getMessage(), e);
        } finally {
            es.shutdown();
        }
    }

    private <T> List<T> optionallyTruncateToLimit(List<T> list, int limit) {
        if (limit > 0) {
            return list.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        } else {
            return list;
        }
    }
}
