package pl.cyfronet.s4e;

import lombok.val;
import pl.cyfronet.s4e.bean.ProductCategory;

import java.util.concurrent.atomic.AtomicInteger;

public class ProductCategoryHelper {
    private static final AtomicInteger COUNT = new AtomicInteger();

    private static final String label = "Product category %d";
    private static final String url = "host:5000/%d";

    public static ProductCategory.ProductCategoryBuilder productCategoryBuilder() {
        val label = nextUnique(ProductCategoryHelper.label);
        val url = nextUnique(ProductCategoryHelper.url);
        return ProductCategory
                .builder()
                .label(label)
                .name(label);
    }

    private static String nextUnique(String format) {
        return String.format(format, COUNT.getAndIncrement());
    }
}
