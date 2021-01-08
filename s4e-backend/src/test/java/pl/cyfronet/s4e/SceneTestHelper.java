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

package pl.cyfronet.s4e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import lombok.val;
import pl.cyfronet.s4e.bean.Legend;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class SceneTestHelper {
    private static final AtomicInteger COUNT = new AtomicInteger();
    private static final String SCENE_KEY_PATTERN = "path/to/%dth.scene";
    private static final String PRODUCT_NAME_PATTERN = "Great %d Product";

    private static final DateTimeFormatter METADATA_SENSING_TIME_PATTERN = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final Map<String, String> DEFAULT_GRANULE_ARTIFACT_RULE = Map.of(
            "default", "quicklook",
            "GeoTiff", "default_artifact"
    );

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ZoneId BASE_ZONE = ZoneId.of("UTC");

    public static String nextUnique(String format) {
        return String.format(format, COUNT.getAndIncrement());
    }

    public static Product.ProductBuilder productBuilder() {
        val current = COUNT.getAndIncrement();
        String displayName = String.format(PRODUCT_NAME_PATTERN, current);
        String name = displayName.replace(" ", "_");
        return Product.builder()
                .name(name)
                .displayName(displayName)
                .description("sth")
                .downloadOnly(false)
                .authorizedOnly(false)
                .accessType(Product.AccessType.OPEN)
                .layerName(name.toLowerCase())
                .granuleArtifactRule(DEFAULT_GRANULE_ARTIFACT_RULE)
                .rank(1000L * current);
    }

    @SneakyThrows
    public static Scene.SceneBuilder sceneBuilder(Product product) {
        return sceneBuilder(product, LocalDateTime.now());
    }

    @SneakyThrows
    public static Scene.SceneBuilder sceneBuilder(Product product, LocalDateTime timestamp) {
        ObjectNode metadataContent = OBJECT_MAPPER.createObjectNode();
        metadataContent.put("format", "GeoTiff");
        metadataContent.put("sensing_time", METADATA_SENSING_TIME_PATTERN.format(timestamp.atZone(BASE_ZONE)));

        return Scene.builder()
                .product(product)
                .sceneKey(nextUnique(SCENE_KEY_PATTERN))
                .footprint(TestGeometryHelper.ANY_POLYGON)
                .legend(Legend.builder()
                        .type("some_type")
                        .build())
                .sceneContent(getDefaultSceneContent())
                .metadataContent(metadataContent);
    }

    public static Scene.SceneBuilder sceneWithMetadataBuilder(Product product, JsonNode metadataContent) {
        return Scene.builder()
                .product(product)
                .sceneKey(nextUnique(SCENE_KEY_PATTERN))
                .sceneContent(getDefaultSceneContent())
                .metadataContent(metadataContent)
                .footprint(TestGeometryHelper.ANY_POLYGON)
                .legend(Legend.builder()
                        .type("some_type")
                        .build());
    }

    private static ObjectNode getDefaultSceneContent() {
        ObjectNode sceneContent = OBJECT_MAPPER.createObjectNode();
        {
            ObjectNode artifacts = sceneContent.putObject("artifacts");
            artifacts.put("default_artifact", "/some/path");
            artifacts.put("other_artifact", "/some/other/path");
        }
        return sceneContent;
    }

    public static JsonNode getMetadataContentWithNumber(long number) {
        return OBJECT_MAPPER.createObjectNode()
                .put("spacecraft", "Sentinel-1A")
                .put("product_type", "GRDH")
                .put("sensor_mode", "IW")
                .put("collection", "S1B_24AU")
                .put("timeliness", "Near Real Time")
                .put("instrument", "OLCI")
                .put("product_level", "L" + number%10)
                .put("processing_level", number%10 + "LC")
                .put("cloud_cover", number)
                .put("polarisation", "Dual VV/VH")
                .put("sensing_time", "2019-11-0" + (number%9 + 1) + "T05:07:42.047432+00:00")
                .put("ingestion_time", "2019-11-0" + (number%9 + 1) + "T05:34:27.000000+00:00")
                .put("relative_orbit_number", "" + number)
                .put("absolute_orbit_number", "0" + number)
                .put("polygon", "55.975475,19.579060 56.395580,15.414984 57.887905,15.835841 57.462494,20.167494")
                .put("format", "GeoTiff" + number)
                .put("schema", "Sentinel-1.metadata.v1.json");
    }

    public static JsonNode getSceneContent() {
        String pathMain = "/Sentinel-1/GRDH/2020-01-04/";
        String pathProducts = "/Sentinel-1/GRDH_Products/2020-01-04/";
        String name = "S1A_IW_GRDH_1SDV_20200104T160956_20200104T161021_030653_038356_BCD9";
        return OBJECT_MAPPER.createObjectNode()
                .put("schema", "Sentinel-1.scene.v1.json")
                .put("product_type", "Sentinel-1-GRDH")
                .set("artifacts", OBJECT_MAPPER.createObjectNode()
                        .put("checksum", pathMain + name + ".SAFE.md5")
                        .put("manifest", pathMain + name + ".manifest.xml")
                        .put("metadata", pathMain + name + ".metadata")
                        .put("quicklook", pathMain + name + ".qlf.tif")
                        .put("product_archive", pathMain + name + ".SAFE.zip")
                        .put("RGB_16b", pathProducts + name + ".RGB.tif")
                        .put("RGBs_8b", pathProducts + name + ".RGBs.tif")
                );
    }

    public static Function<LocalDateTime, Scene> toScene(Product product) {
        return timestamp -> sceneBuilder(product, timestamp).build();
    }
}
