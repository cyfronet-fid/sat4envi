package pl.cyfronet.s4e.geoserver.op;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.SceneTestHelper;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.properties.S3Properties;
import pl.cyfronet.s4e.util.GeometryUtil;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Profile({"integration"})
@Component
@Slf4j
public class SeedProductsTest {
    private static final DateTimeFormatter DATE_TIME_PATTERN = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter YEAR_PATTERN = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter TIME_PATTERN = DateTimeFormatter.ofPattern("HHmm");

    private static final DateTimeFormatter METADATA_SENSING_TIME_PATTERN = DateTimeFormatter.ISO_DATE_TIME;
    private static final Map<String, String> DEFAULT_GRANULE_ARTIFACT_RULE = Map.of("GeoTiff", "default_artifact");

    @Autowired
    private SceneRepository sceneRepository;
    @Autowired
    private S3Properties s3Properties;
    @Autowired
    private S3Client s3Client;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private GeometryUtil geom;
    @Autowired
    private ObjectMapper objectMapper;

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
                        .layerName("108m")
                        .granuleArtifactRule(DEFAULT_GRANULE_ARTIFACT_RULE)
                        .build(),
                Product.builder()
                        .name("Setvak")
                        .displayName("Setvak")
                        .description("Obraz satelitarny Meteosat w kanale 10.8 µm z paletą barwną do analizy powierzchni wysokich chmur konwekcyjnych – obszar Europy Centralnej.")
                        .layerName("setvak")
                        .granuleArtifactRule(DEFAULT_GRANULE_ARTIFACT_RULE)
                        .build()
        });
        productRepository.saveAll(products);

        LocalDateTime startInclusive = LocalDateTime.of(2018, 10, 4, 0, 0);
        LocalDateTime endExclusive = startInclusive.plusDays(1);

        Geometry footprint;
        try {
            footprint = geom.parseWKT("POLYGON((0 -10379291.247,0 0,10463673.528 0,10463673.528 -10379291.247,0 -10379291.247))", GeometryUtil.FACTORY_3857);
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
                        .build()
        );

        log.info("Seeding Scenes, from: " + startInclusive.toString() + " to " + endExclusive.toString());
        for (val product : products) {
            seedScenes(product, productParams.get(product.getName()));
        }
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

            ObjectNode sceneContent = objectMapper.createObjectNode();
            {
                ObjectNode artifacts = sceneContent.putObject("artifacts");
                artifacts.put("default_artifact", "/" + s3Path);
            }
            ObjectNode metadataContent = objectMapper.createObjectNode();
            metadataContent.put("format", "GeoTiff");
            metadataContent.put("sensing_time", METADATA_SENSING_TIME_PATTERN.format(timestamp));

            val scene = Scene.builder()
                    .product(product)
                    .sceneKey(SceneTestHelper.nextUnique("path/to/%dth.scene"))
                    .footprint(params.getFootprint())
                    .sceneContent(sceneContent)
                    .metadataContent(metadataContent)
                    .build();

            if (objectExists(s3Path)) {
                sceneRepository.save(scene);
            } else {
                log.info("Key doesn't exist: '" + s3Path + "', omitting scene");
            }

            if ((i + 1) % 100 == 0) {
                log.info((i + 1) + "/" + count + " scenes of product '" + product.getName() + "' processed");
            }
        }
    }

    private boolean objectExists(String key) {
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(key)
                .build();
        try {
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }
}
