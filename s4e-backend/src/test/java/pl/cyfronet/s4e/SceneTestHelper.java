package pl.cyfronet.s4e;

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
                .granulePath("mailto://bucket/some/path")
                .footprint(TestGeometryHelper.ANY_POLYGON);
    }

    public static Function<LocalDateTime, Scene> toScene(Product product) {
        return timestamp -> sceneBuilder(product).timestamp(timestamp).build();
    }
}
