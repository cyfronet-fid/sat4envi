package pl.cyfronet.s4e;

import lombok.val;
import pl.cyfronet.s4e.bean.ProductCategory;

import java.util.concurrent.atomic.AtomicInteger;

public class ProductCategoryHelper {
    private static final AtomicInteger COUNT = new AtomicInteger();

    private static final String label = "Product category %d";
    private static final String iconName = "icon %d SVG";

    public static ProductCategory.ProductCategoryBuilder productCategoryBuilder() {
        val label = nextUnique(ProductCategoryHelper.label);
        val iconName = nextUnique(ProductCategoryHelper.iconName);
        return ProductCategory
                .builder()
                .label(label)
                .name(label)
                .iconName(iconName);
    }

    private static String nextUnique(String format) {
        return String.format(format, COUNT.getAndIncrement());
    }
}
