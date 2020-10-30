package pl.cyfronet.s4e.geoserver.op;

import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.data.repository.ProductRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Profile({"integration"})
@Component
@Slf4j
public class SeedProductsTest {
    private static final Map<String, String> DEFAULT_GRANULE_ARTIFACT_RULE = Map.of("GeoTiff", "default_artifact");

    @Autowired
    private ProductRepository productRepository;

    @Builder
    @Value
    private static class ProductParams {
        LocalDateTime startInclusive;
        LocalDateTime endExclusive;
        String s3PathFormat;
        Geometry footprint;
        @Builder.Default
        Duration increment = Duration.ofHours(1);
    }

    public void prepareDb() {
        seedProductsMinioDataV1();
    }

    private void seedProductsMinioDataV1() {
        log.info("Seeding Products: test");
        List<Product> products = Arrays.asList(new Product[]{
                Product.builder()
                        .name("108m")
                        .displayName("108m")
                        .description("Obraz satelitarny Meteosat dla obszaru Europy w kanale 10.8 µm z zastosowanie maskowanej palety barw dla obszarów mórz i lądów.")
                        .accessType(Product.AccessType.OPEN)
                        .layerName("108m")
                        .granuleArtifactRule(DEFAULT_GRANULE_ARTIFACT_RULE)
                        .build(),
                Product.builder()
                        .name("Setvak")
                        .displayName("Setvak")
                        .description("Obraz satelitarny Meteosat w kanale 10.8 µm z paletą barwną do analizy powierzchni wysokich chmur konwekcyjnych – obszar Europy Centralnej.")
                        .accessType(Product.AccessType.OPEN)
                        .layerName("setvak")
                        .granuleArtifactRule(DEFAULT_GRANULE_ARTIFACT_RULE)
                        .build()
        });
        productRepository.saveAll(products);
    }
}
