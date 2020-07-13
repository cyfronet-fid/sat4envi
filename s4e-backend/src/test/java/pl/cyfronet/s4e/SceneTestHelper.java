package pl.cyfronet.s4e;

import com.fasterxml.jackson.databind.JsonNode;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class SceneTestHelper {
    private static final AtomicInteger COUNT = new AtomicInteger();
    private static final String SCENE_KEY_PATTERN = "path/to/%dth.scene";
    private static final String PRODUCT_NAME_PATTERN = "Great %d Product";

    public static String nextUnique(String format) {
        return String.format(format, COUNT.getAndIncrement());
    }

    public static Product.ProductBuilder productBuilder() {
        String displayName = nextUnique(PRODUCT_NAME_PATTERN);
        String name = displayName.replace(" ", "_");
        return Product.builder()
                .name(name)
                .displayName(displayName)
                .description("sth")
                .layerName(name.toLowerCase());
    }

    public static Scene.SceneBuilder sceneBuilder(Product product) {
        return Scene.builder()
                .product(product)
                .sceneKey(nextUnique(SCENE_KEY_PATTERN))
                .timestamp(LocalDateTime.now())
                .s3Path("some/path")
                .granulePath("mailto://s4e-test-1/some/path")
                .footprint(TestGeometryHelper.ANY_POLYGON);
    }

    public static Scene.SceneBuilder sceneWithMetadataBuilder(Product product, JsonNode jsonNode) {
        return Scene.builder()
                .product(product)
                .sceneKey(nextUnique(SCENE_KEY_PATTERN))
                .timestamp(LocalDateTime.now())
                .s3Path("some/path")
                .granulePath("mailto://s4e-test-1/some/path")
                .metadataContent(jsonNode)
                .footprint(TestGeometryHelper.ANY_POLYGON);
    }

    public static String getMetaDataWithNumber(long number) {
        return "{\n" +
                "   \"spacecraft\": \"Sentinel-1A\",\n" +
                "   \"product_type\": \"GRDH\",\n" +
                "   \"sensor_mode\": \"IW\",\n" +
                "   \"collection\": \"S1B_24AU\",\n" +
                "   \"timeliness\": \"Near Real Time\",\n" +
                "   \"instrument\": \"OLCI\",\n" +
                "   \"product_level\": \"L" + number%10 + "\",\n" +
                "   \"processing_level\": \"" + number%10 + "LC\",\n" +
                "   \"cloud_cover\": " + number + ",\n" +
                "   \"polarisation\": \"Dual VV/VH\",\n" +
                "   \"sensing_time\": \"2019-11-0" + number%10 + "T05:07:42.047432+00:00\",\n" +
                "   \"ingestion_time\": \"2019-11-0" + number%10 + "T05:34:27.000000+00:00\",\n" +
                "   \"relative_orbit_number\": \"" + number + "\",\n" +
                "   \"absolute_orbit_number\": \"0" + number + "\",\n" +
                "   \"polygon\": \"55.975475,19.579060 56.395580,15.414984 57.887905,15.835841 57.462494,20.167494\",\n" +
                "   \"format\": \"GeoTiff" + number + "\",\n" +
                "   \"schema\": \"Sentinel-1.metadata.v1.json\"\n" +
                "}";
    }

    public static String getSceneContent(){
        return " {\"schema\": \"Sentinel-1.scene.v1.json\"," +
                " \"artifacts\": " +
                "{\"RGB_16b\": \"/Sentinel-1/GRDH_Products/2020-01-04/S1A_IW_GRDH_1SDV_20200104T160956_20200104T161021_030653_038356_BCD9.RGB.tif\"," +
                " \"RGBs_8b\": \"/Sentinel-1/GRDH_Products/2020-01-04/S1A_IW_GRDH_1SDV_20200104T160956_20200104T161021_030653_038356_BCD9.RGBs.tif\"," +
                " \"checksum\": \"/Sentinel-1/GRDH/2020-01-04/S1A_IW_GRDH_1SDV_20200104T160956_20200104T161021_030653_038356_BCD9.SAFE.md5\", " +
                "\"manifest\": \"/Sentinel-1/GRDH/2020-01-04/S1A_IW_GRDH_1SDV_20200104T160956_20200104T161021_030653_038356_BCD9.manifest.xml\"," +
                " \"metadata\": \"/Sentinel-1/GRDH/2020-01-04/S1A_IW_GRDH_1SDV_20200104T160956_20200104T161021_030653_038356_BCD9.metadata\", " +
                "\"quicklook\": \"/Sentinel-1/GRDH/2020-01-04/S1A_IW_GRDH_1SDV_20200104T160956_20200104T161021_030653_038356_BCD9.qlf.tif\"," +
                " \"product_archive\": \"/Sentinel-1/GRDH/2020-01-04/S1A_IW_GRDH_1SDV_20200104T160956_20200104T161021_030653_038356_BCD9.SAFE.zip\"}, " +
                "\"product_type\": \"Sentinel-1-GRDH\"} ";
    }

    public static Function<LocalDateTime, Scene> toScene(Product product) {
        return timestamp -> sceneBuilder(product).timestamp(timestamp).build();
    }
}
