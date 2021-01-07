/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
        List<Product> products = Arrays.asList(
                Product.builder()
                        .name("108m")
                        .displayName("108m")
                        .description("Obraz satelitarny Meteosat dla obszaru Europy w kanale 10.8 µm z zastosowanie maskowanej palety barw dla obszarów mórz i lądów.")
                        .downloadOnly(false)
                        .authorizedOnly(false)
                        .accessType(Product.AccessType.OPEN)
                        .layerName("108m")
                        .granuleArtifactRule(DEFAULT_GRANULE_ARTIFACT_RULE)
                        .rank(1000L)
                        .build(),
                Product.builder()
                        .name("Setvak")
                        .displayName("Setvak")
                        .description("Obraz satelitarny Meteosat w kanale 10.8 µm z paletą barwną do analizy powierzchni wysokich chmur konwekcyjnych – obszar Europy Centralnej.")
                        .downloadOnly(false)
                        .authorizedOnly(false)
                        .accessType(Product.AccessType.OPEN)
                        .layerName("setvak")
                        .granuleArtifactRule(DEFAULT_GRANULE_ARTIFACT_RULE)
                        .rank(2000L)
                        .build()
        );
        productRepository.saveAll(products);
    }
}
